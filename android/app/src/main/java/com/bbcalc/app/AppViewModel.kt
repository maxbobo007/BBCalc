package com.bbcalc.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
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
    val search: String = "",
    val rates: Map<String, Double> = Currencies.builtinRates,
    val rateState: RateState = RateState.BUILTIN,
    val rateSource: String = "",
    val lastUpdate: Long = 0L,
    val updating: Boolean = false,
)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("bbcalc", 0)
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init {
        var st = UiState(
            baseCurrency = prefs.getString("base", "CNY") ?: "CNY",
            pinned = prefs.getStringSet("pinned", emptySet())?.toSet() ?: emptySet(),
        )
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
        fetchRates()
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

    fun togglePin(code: String) {
        val st = _state.value
        val pinned = if (code in st.pinned) st.pinned - code else st.pinned + code
        _state.value = st.copy(pinned = pinned)
        prefs.edit().putStringSet("pinned", pinned).apply()
    }

    fun setSearch(q: String) {
        _state.value = _state.value.copy(search = q)
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
