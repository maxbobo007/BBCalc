package com.bbcalc.app

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

enum class RateState { BUILTIN, CACHED, LIVE }

data class UiState(
    val expression: String = "100",
    val isError: Boolean = false,
    val preview: Double = 100.0,
    val baseCurrency: String = "CNY",
    val pinned: Set<String> = emptySet(),
    val displayCurrencies: Set<String> = Currencies.all.map { it.code }.toSet(),
    val themeMode: String = "auto", // auto | light | dark
    val search: String = "",
    val rates: Map<String, Double> = Currencies.builtinRates,
    val rateState: RateState = RateState.BUILTIN,
    val rateSource: String = "",
    val lastUpdate: Long = 0L,
    val updating: Boolean = false,
    val userName: String? = null,
    val userEmail: String? = null,
)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("bbcalc", 0)
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    // ── Firebase（google-services.json 缺失时优雅降级为不可用）──
    private val auth: FirebaseAuth? = try {
        if (FirebaseApp.getApps(app).isNotEmpty()) FirebaseAuth.getInstance() else null
    } catch (e: Exception) { null }

    private val firestore: FirebaseFirestore? = try {
        if (auth != null) FirebaseFirestore.getInstance() else null
    } catch (e: Exception) { null }

    /** Google 登录所需的 Web Client ID（由 google-services 插件生成的资源，缺失则为 null） */
    val webClientId: String? = run {
        val id = app.resources.getIdentifier("default_web_client_id", "string", app.packageName)
        if (id != 0) app.getString(id) else null
    }

    val authAvailable: Boolean get() = auth != null && webClientId != null

    private var syncJob: Job? = null

    init {
        var st = UiState(
            baseCurrency = prefs.getString("base", "CNY") ?: "CNY",
            pinned = prefs.getStringSet("pinned", emptySet())?.toSet() ?: emptySet(),
            themeMode = prefs.getString("themeMode", "auto") ?: "auto",
        )
        prefs.getStringSet("displayCurrencies", null)?.let { saved ->
            val valid = saved.filter { Currencies.byCode.containsKey(it) }.toSet()
            st = st.copy(displayCurrencies = valid)
        }
        // 启动时优先使用上次成功获取并缓存的汇率（与网页版策略一致）
        prefs.getString("cachedRates", null)?.let { json ->
            try {
                val o = JSONObject(json)
                val r = o.getJSONObject("rates")
                val m = HashMap<String, Double>()
                for (k in r.keys()) {
                    val v = r.optDouble(k)
                    if (!v.isNaN()) m[k.uppercase()] = v
                }
                if (m.containsKey("USD")) {
                    st = st.copy(
                        rates = Currencies.builtinRates + m,
                        rateState = RateState.CACHED,
                        rateSource = o.optString("source"),
                        lastUpdate = o.optLong("ts"),
                    )
                }
            } catch (e: Exception) { /* 缓存损坏则忽略 */ }
        }
        _state.value = st

        auth?.addAuthStateListener { a ->
            val user = a.currentUser
            _state.value = _state.value.copy(
                userName = user?.displayName,
                userEmail = user?.email,
            )
            if (user != null) loadFromCloud(user.uid)
        }

        fetchRates()
    }

    private fun toast(resId: Int) {
        Toast.makeText(getApplication(), resId, Toast.LENGTH_SHORT).show()
    }

    // ── 计算器 ──

    fun append(ch: String) {
        val st = _state.value
        var e = if (st.isError) "" else st.expression // 出错后直接开始新输入
        e = if (e == "0" && ch != ".") ch else e + ch
        setExpression(e)
    }

    fun backspace() {
        val st = _state.value
        if (st.isError) { clear(); return }
        if (st.expression.isNotEmpty()) setExpression(st.expression.dropLast(1))
    }

    fun clear() {
        _state.value = _state.value.copy(expression = "", isError = false, preview = 0.0)
    }

    fun toggleSign() {
        val st = _state.value
        if (st.preview != 0.0 && !st.isError) {
            val v = CalcEngine.round12(-st.preview)
            _state.value = st.copy(expression = CalcEngine.numToStr(v), preview = v, isError = false)
        }
    }

    fun percent() {
        val st = _state.value
        if (st.preview != 0.0 && !st.isError) {
            val v = CalcEngine.round12(st.preview / 100.0)
            _state.value = st.copy(expression = CalcEngine.numToStr(v), preview = v, isError = false)
        }
    }

    fun calculate() {
        val st = _state.value
        val v = CalcEngine.eval(st.expression)
        _state.value = if (v == null) {
            st.copy(isError = true, expression = "", preview = 0.0)
        } else {
            st.copy(expression = CalcEngine.numToStr(v), preview = v, isError = false)
        }
    }

    private fun setExpression(e: String) {
        val st = _state.value
        // 未输完时保留上一个有效值，避免换算列表闪 0（与网页版一致）
        val p = CalcEngine.evalPreview(e) ?: st.preview
        _state.value = st.copy(expression = e, isError = false, preview = p)
    }

    // ── 货币 ──

    /** 点卡片切换基准货币：显示值变为该货币的换算金额（与网页版一致） */
    fun switchBase(code: String) {
        val st = _state.value
        if (code == st.baseCurrency) return
        val baseRate = st.rates[st.baseCurrency] ?: return
        val rate = st.rates[code] ?: return
        var next = st.copy(baseCurrency = code)
        if (st.preview != 0.0 && !st.isError) {
            val v = st.preview / baseRate * rate
            val e = String.format(Locale.US, "%.2f", v)
            next = next.copy(expression = e, preview = CalcEngine.evalPreview(e) ?: v, isError = false)
        }
        _state.value = next
        prefs.edit().putString("base", code).apply()
    }

    /** 设置里的默认基准货币：切换 + 同步到云端（对应网页版 defaultCurrency 字段） */
    fun setDefaultBase(code: String) {
        switchBase(code)
        scheduleCloudSave()
    }

    fun togglePin(code: String) {
        val st = _state.value
        val pinned = if (code in st.pinned) st.pinned - code else st.pinned + code
        _state.value = st.copy(pinned = pinned)
        prefs.edit().putStringSet("pinned", pinned).apply()
        scheduleCloudSave()
    }

    fun toggleDisplayCurrency(code: String) {
        val st = _state.value
        val set = if (code in st.displayCurrencies) st.displayCurrencies - code
                  else st.displayCurrencies + code
        _state.value = st.copy(displayCurrencies = set)
        prefs.edit().putStringSet("displayCurrencies", set).apply()
        scheduleCloudSave()
    }

    fun setThemeMode(mode: String) {
        _state.value = _state.value.copy(themeMode = mode)
        prefs.edit().putString("themeMode", mode).apply()
        scheduleCloudSave()
    }

    fun setSearch(q: String) {
        _state.value = _state.value.copy(search = q)
    }

    // ── 登录 / 云同步（与网页版共用 users/{uid}/config/settings 文档）──

    fun signInWithGoogleToken(idToken: String) {
        val a = auth ?: return
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        a.signInWithCredential(credential)
            .addOnFailureListener { toast(R.string.sign_in_failed) }
    }

    fun signOut() {
        auth?.signOut()
    }

    fun deleteAccount() {
        val a = auth ?: return
        val user = a.currentUser ?: return
        val doc = firestore?.document("users/${user.uid}/config/settings")
        val finish = {
            user.delete()
                .addOnSuccessListener { toast(R.string.delete_done) }
                .addOnFailureListener { toast(R.string.delete_fail) }
            Unit
        }
        if (doc != null) {
            doc.delete().addOnCompleteListener { finish() }
        } else {
            finish()
        }
    }

    private fun loadFromCloud(uid: String) {
        val doc = firestore?.document("users/$uid/config/settings") ?: return
        doc.get().addOnSuccessListener { snap ->
            if (!snap.exists()) { scheduleCloudSave(); return@addOnSuccessListener }
            var st = _state.value

            (snap.get("pinnedCurrencies") as? List<*>)?.let { list ->
                val pinned = list.filterIsInstance<String>()
                    .filter { Currencies.byCode.containsKey(it) }.toSet()
                st = st.copy(pinned = pinned)
                prefs.edit().putStringSet("pinned", pinned).apply()
            }
            (snap.get("displayCurrencies") as? List<*>)?.let { list ->
                val display = list.filterIsInstance<String>()
                    .filter { Currencies.byCode.containsKey(it) }.toSet()
                st = st.copy(displayCurrencies = display)
                prefs.edit().putStringSet("displayCurrencies", display).apply()
            }
            (snap.getString("themeMode"))?.let { mode ->
                if (mode in listOf("auto", "light", "dark")) {
                    st = st.copy(themeMode = mode)
                    prefs.edit().putString("themeMode", mode).apply()
                }
            }
            (snap.getString("defaultCurrency"))?.let { code ->
                if (Currencies.byCode.containsKey(code)) {
                    st = st.copy(baseCurrency = code)
                    prefs.edit().putString("base", code).apply()
                }
            }
            _state.value = st
        }
    }

    /** 防抖 2 秒后上传（merge 写入，不覆盖网页版独有的 currencyOrder / appLanguage 字段） */
    private fun scheduleCloudSave() {
        val a = auth ?: return
        val user = a.currentUser ?: return
        val doc = firestore?.document("users/${user.uid}/config/settings") ?: return
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            delay(2000)
            val st = _state.value
            val data = mapOf(
                "pinnedCurrencies" to st.pinned.toList(),
                "displayCurrencies" to st.displayCurrencies.toList(),
                "defaultCurrency" to st.baseCurrency,
                "themeMode" to st.themeMode,
                "updatedAt" to System.currentTimeMillis(),
            )
            doc.set(data, SetOptions.merge())
        }
    }

    // ── 汇率获取：currencyapi (CDN) → frankfurter，成功后写缓存；全失败保持现有数据 ──

    fun fetchRates() {
        if (_state.value.updating) return
        _state.value = _state.value.copy(updating = true)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { tryFetch() }
            if (result != null) {
                val (rates, source) = result
                val now = System.currentTimeMillis()
                _state.value = _state.value.copy(
                    rates = Currencies.builtinRates + rates,
                    rateState = RateState.LIVE,
                    rateSource = source,
                    lastUpdate = now,
                    updating = false,
                )
                try {
                    val o = JSONObject()
                    o.put("rates", JSONObject(rates as Map<*, *>))
                    o.put("ts", now)
                    o.put("source", source)
                    prefs.edit().putString("cachedRates", o.toString()).apply()
                } catch (e: Exception) { /* 忽略 */ }
            } else {
                _state.value = _state.value.copy(updating = false)
            }
        }
    }

    private fun tryFetch(): Pair<Map<String, Double>, String>? {
        val apis = listOf(
            Triple(
                "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.json",
                "usd", "Currency API (CDN)"
            ),
            Triple(
                "https://api.frankfurter.app/latest?from=USD",
                "rates", "Frankfurter (ECB)"
            ),
        )
        for ((url, key, name) in apis) {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(text).getJSONObject(key)
                val m = HashMap<String, Double>()
                for (k in obj.keys()) {
                    val v = obj.optDouble(k)
                    if (!v.isNaN()) m[k.uppercase()] = v
                }
                if (m.size > 10) {
                    m["USD"] = 1.0
                    return m to name
                }
            } catch (e: Exception) {
                // 尝试下一个数据源
            }
        }
        return null
    }
}
