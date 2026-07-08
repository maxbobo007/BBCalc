package com.bbcalc.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 调色板 —— 与网页版 CSS 变量一一对应 */
data class Palette(
    val bg: Color,
    val surface: Color,
    val surfaceC: Color,
    val surfaceCh: Color,
    val onSurface: Color,
    val onSv: Color,
    val primary: Color,
    val primaryC: Color,
    val onPc: Color,
    val errC: Color,
    val onErr: Color,
    val outline: Color,
)

val DarkPalette = Palette(
    bg = Color(0xFF0E0D12),
    surface = Color(0xFF1C1B1F),
    surfaceC = Color(0xFF2B2930),
    surfaceCh = Color(0xFF36343B),
    onSurface = Color(0xFFE6E1E5),
    onSv = Color(0xFF938F99),
    primary = Color(0xFF4285F4),
    primaryC = Color(0x2E4285F4),
    onPc = Color(0xFFA8C7FA),
    errC = Color(0x26FF6450),
    onErr = Color(0xFFFFB4AB),
    outline = Color(0x14CAC4D0),
)

val LightPalette = Palette(
    bg = Color(0xFFF3F3FA),
    surface = Color(0xFFFFFFFF),
    surfaceC = Color(0xFFEDEDF4),
    surfaceCh = Color(0xFFE2E1E8),
    onSurface = Color(0xFF1C1B1F),
    onSv = Color(0xFF49454F),
    primary = Color(0xFF1A73E8),
    primaryC = Color(0x1A1A73E8),
    onPc = Color(0xFF1A4FA0),
    errC = Color(0x1AC82814),
    onErr = Color(0xFFB3261E),
    outline = Color(0x17494F5F),
)

val LocalPalette = staticCompositionLocalOf { DarkPalette }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val palette = if (isSystemInDarkTheme()) DarkPalette else LightPalette
            CompositionLocalProvider(LocalPalette provides palette) {
                MaterialTheme {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen(vm: AppViewModel = viewModel()) {
    val p = LocalPalette.current
    val st by vm.state.collectAsState()

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(p.bg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(10.dp)
    ) {
        val landscape = maxWidth > maxHeight
        if (landscape) {
            Row(Modifier.fillMaxSize()) {
                CalculatorPanel(st, vm, Modifier.weight(46f))
                Spacer(Modifier.width(10.dp))
                RatesPanel(st, vm, Modifier.weight(54f))
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                RatesPanel(st, vm, Modifier.weight(1f))
                Spacer(Modifier.height(10.dp))
                CalculatorPanel(st, vm, Modifier.fillMaxWidth())
            }
        }
    }
}

// ── 汇率面板 ──

@Composable
fun RatesPanel(st: UiState, vm: AppViewModel, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val chinese = Locale.getDefault().language == "zh"

    val q = st.search.trim().lowercase()
    val filtered = Currencies.all
        .filter { it.code != st.baseCurrency }
        .filter {
            q.isEmpty() || it.code.lowercase().contains(q) ||
                it.zh.contains(q) || it.en.lowercase().contains(q)
        }
    val visible = filtered.filter { it.code in st.pinned } + filtered.filter { it.code !in st.pinned }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = p.surface,
    ) {
        Column(Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.conversion_title),
                    color = p.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.weight(1f))
                SearchBox(st.search, vm::setSearch)
            }
            Spacer(Modifier.height(10.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(7.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                items(visible, key = { it.code }) { cur ->
                    CurrencyCard(cur, st, vm, chinese)
                }
            }
        }
    }
}

@Composable
fun SearchBox(value: String, onChange: (String) -> Unit) {
    val p = LocalPalette.current
    Surface(
        shape = RoundedCornerShape(50),
        color = p.surfaceC,
        modifier = Modifier.width(150.dp),
    ) {
        Box(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            if (value.isEmpty()) {
                Text(stringResource(R.string.search_hint), color = p.onSv, fontSize = 13.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(color = p.onSurface, fontSize = 13.sp),
                cursorBrush = SolidColor(p.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun CurrencyCard(cur: CurrencyInfo, st: UiState, vm: AppViewModel, chinese: Boolean) {
    val p = LocalPalette.current
    val pinned = cur.code in st.pinned
    val rate = st.rates[cur.code]
    val baseRate = st.rates[st.baseCurrency]
    val value = if (rate != null && baseRate != null && st.preview != 0.0 && !st.isError) {
        st.preview / baseRate * rate
    } else 0.0

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (pinned) p.primaryC else p.surfaceC,
        modifier = Modifier.clickable { vm.switchBase(cur.code) },
    ) {
        Box(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 7.dp)) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(end = 20.dp),
                ) {
                    Text(
                        "${cur.flag} ${cur.code}",
                        color = p.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        if (chinese) cur.zh else cur.en,
                        color = p.onSv, fontSize = 10.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    String.format(Locale.US, "%,.2f", value),
                    color = p.onPc, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End, maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Text(
                "📌",
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable { vm.togglePin(cur.code) }
                    .padding(6.dp),
                color = if (pinned) p.onPc else p.onSv.copy(alpha = 0.45f),
            )
        }
    }
}

// ── 计算器面板 ──

@Composable
fun CalculatorPanel(st: UiState, vm: AppViewModel, modifier: Modifier = Modifier) {
    val p = LocalPalette.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = p.surface,
    ) {
        Column(Modifier.padding(14.dp).verticalScroll(rememberScrollState())) {
            // 标题 + 基准货币徽标
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.title),
                    color = p.onSurface, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(50), color = p.primaryC) {
                    Text(
                        st.baseCurrency,
                        color = p.onPc, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            // 显示屏
            Surface(shape = RoundedCornerShape(14.dp), color = p.surfaceC, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) {
                    Text(
                        if (st.isError) stringResource(R.string.calc_error) else st.expression,
                        color = p.onSv, fontSize = 13.sp, textAlign = TextAlign.End,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth().height(18.dp),
                    )
                    Text(
                        CalcEngine.formatNumber(st.preview),
                        color = p.onSurface, fontSize = 32.sp, fontWeight = FontWeight.Light,
                        textAlign = TextAlign.End, maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            // 键盘
            KeypadRow {
                CalcButton("C", KeyKind.FUNCTION, Modifier.weight(1f)) { vm.clear() }
                CalcButton("+/−", KeyKind.FUNCTION, Modifier.weight(1f)) { vm.toggleSign() }
                CalcButton("%", KeyKind.FUNCTION, Modifier.weight(1f)) { vm.percent() }
                CalcButton("⌫", KeyKind.BACKSPACE, Modifier.weight(1f)) { vm.backspace() }
            }
            KeypadRow {
                CalcButton("7", KeyKind.NUMBER, Modifier.weight(1f)) { vm.append("7") }
                CalcButton("8", KeyKind.NUMBER, Modifier.weight(1f)) { vm.append("8") }
                CalcButton("9", KeyKind.NUMBER, Modifier.weight(1f)) { vm.append("9") }
                CalcButton("÷", KeyKind.OPERATOR, Modifier.weight(1f)) { vm.append("/") }
            }
            KeypadRow {
                CalcButton("4", KeyKind.NUMBER, Modifier.weight(1f)) { vm.append("4") }
                CalcButton("5", KeyKind.NUMBER, Modifier.weight(1f)) { vm.append("5") }
                CalcButton("6", KeyKind.NUMBER, Modifier.weight(1f)) { vm.append("6") }
                CalcButton("×", KeyKind.OPERATOR, Modifier.weight(1f)) { vm.append("*") }
            }
            KeypadRow {
                CalcButton("1", KeyKind.NUMBER, Modifier.weight(1f)) { vm.append("1") }
                CalcButton("2", KeyKind.NUMBER, Modifier.weight(1f)) { vm.append("2") }
                CalcButton("3", KeyKind.NUMBER, Modifier.weight(1f)) { vm.append("3") }
                CalcButton("−", KeyKind.OPERATOR, Modifier.weight(1f)) { vm.append("-") }
            }
            KeypadRow {
                CalcButton("0", KeyKind.NUMBER, Modifier.weight(2.07f)) { vm.append("0") }
                CalcButton(".", KeyKind.NUMBER, Modifier.weight(1f)) { vm.append(".") }
                CalcButton("+", KeyKind.OPERATOR, Modifier.weight(1f)) { vm.append("+") }
            }
            KeypadRow {
                CalcButton("=", KeyKind.EQUALS, Modifier.weight(1f)) { vm.calculate() }
            }

            // 汇率信息行
            Spacer(Modifier.height(6.dp))
            RateInfoLine(st, vm)
        }
    }
}

@Composable
fun KeypadRow(content: @Composable RowScope.() -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 7.dp),
        content = content,
    )
}

enum class KeyKind { NUMBER, FUNCTION, OPERATOR, EQUALS, BACKSPACE }

@Composable
fun CalcButton(label: String, kind: KeyKind, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val p = LocalPalette.current
    val bg = when (kind) {
        KeyKind.NUMBER -> p.surfaceC
        KeyKind.FUNCTION -> p.surfaceCh
        KeyKind.OPERATOR -> p.primaryC
        KeyKind.EQUALS -> p.primary
        KeyKind.BACKSPACE -> p.errC
    }
    val fg = when (kind) {
        KeyKind.NUMBER -> p.onSurface
        KeyKind.FUNCTION -> p.onSv
        KeyKind.OPERATOR -> p.onPc
        KeyKind.EQUALS -> Color.White
        KeyKind.BACKSPACE -> p.onErr
    }
    val fontSize = when (kind) {
        KeyKind.OPERATOR -> 22.sp
        KeyKind.FUNCTION, KeyKind.BACKSPACE -> 16.sp
        else -> 20.sp
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        modifier = modifier.height(44.dp).clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(label, color = fg, fontSize = fontSize)
        }
    }
}

@Composable
fun RateInfoLine(st: UiState, vm: AppViewModel) {
    val p = LocalPalette.current
    val status = when {
        st.updating -> stringResource(R.string.updating)
        st.rateState == RateState.LIVE -> stringResource(R.string.status_live)
        st.rateState == RateState.CACHED -> stringResource(R.string.status_cached)
        else -> stringResource(R.string.status_offline)
    }
    val source = if (st.rateState == RateState.BUILTIN) {
        stringResource(R.string.builtin_data) + " (2025-01)"
    } else st.rateSource
    val time = if (st.lastUpdate > 0) {
        SimpleDateFormat("M/d HH:mm", Locale.getDefault()).format(Date(st.lastUpdate))
    } else ""

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            listOf(time, status, "·", source).filter { it.isNotEmpty() }.joinToString(" "),
            color = p.onSv, fontSize = 10.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.Transparent,
            modifier = Modifier.clickable { vm.fetchRates() },
        ) {
            Text(
                stringResource(R.string.update),
                color = p.onPc, fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            )
        }
    }
}
