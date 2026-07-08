package com.bbcalc.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.Locale

@Composable
fun SettingsScreen(
    st: UiState,
    vm: AppViewModel,
    onClose: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {
    val p = LocalPalette.current
    val chinese = Locale.getDefault().language == "zh"
    var showBasePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }

    val q = search.trim().lowercase()
    val currencyList = Currencies.all.filter {
        q.isEmpty() || it.code.lowercase().contains(q) ||
            it.zh.contains(q) || it.en.lowercase().contains(q)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(p.bg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(10.dp)
    ) {
        Surface(shape = RoundedCornerShape(20.dp), color = p.surface, modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                Modifier.padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                // 标题栏
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.settings),
                            color = p.onSurface, fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.weight(1f))
                        Surface(
                            shape = RoundedCornerShape(50), color = p.surfaceC,
                            modifier = Modifier.clickable(onClick = onClose),
                        ) {
                            Text(
                                "×", color = p.onSv, fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                            )
                        }
                    }
                }

                // 账号同步
                item { SectionTitle(R.string.account_sync) }
                item {
                    if (st.userEmail == null) {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(12.dp), color = p.surfaceC,
                                modifier = Modifier.clickable(onClick = onSignIn),
                            ) {
                                Text(
                                    stringResource(R.string.sign_in_google),
                                    color = p.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                stringResource(
                                    if (vm.authAvailable) R.string.sign_in_hint
                                    else R.string.sign_in_unavailable
                                ),
                                color = p.onSv, fontSize = 11.sp,
                            )
                        }
                    } else {
                        Column {
                            Text(st.userName ?: "", color = p.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(st.userEmail, color = p.onSv, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp), color = p.surfaceC,
                                    modifier = Modifier.clickable(onClick = onSignOut),
                                ) {
                                    Text(
                                        stringResource(R.string.sign_out),
                                        color = p.onSurface, fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp), color = p.errC,
                                    modifier = Modifier.clickable { showDeleteConfirm = true },
                                ) {
                                    Text(
                                        stringResource(R.string.delete_account),
                                        color = p.onErr, fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                // 主题外观
                item { SectionTitle(R.string.theme) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeButton("light", R.string.theme_light, st.themeMode, vm, Modifier.weight(1f))
                        ThemeButton("auto", R.string.theme_auto, st.themeMode, vm, Modifier.weight(1f))
                        ThemeButton("dark", R.string.theme_dark, st.themeMode, vm, Modifier.weight(1f))
                    }
                }

                // 默认基准货币
                item { SectionTitle(R.string.default_base) }
                item {
                    val cur = Currencies.byCode[st.baseCurrency]
                    Surface(
                        shape = RoundedCornerShape(12.dp), color = p.surfaceC,
                        modifier = Modifier.fillMaxWidth().clickable { showBasePicker = true },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Text(
                                "${cur?.flag ?: ""} ${st.baseCurrency}  ${Currencies.name(st.baseCurrency, chinese)}",
                                color = p.onSurface, fontSize = 15.sp,
                            )
                            Spacer(Modifier.weight(1f))
                            Text("▾", color = p.onSv, fontSize = 14.sp)
                        }
                    }
                }

                // 选择要显示的货币
                item { SectionTitle(R.string.select_currencies) }
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp), color = p.surfaceC,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        BasicTextField(
                            value = search,
                            onValueChange = { search = it },
                            singleLine = true,
                            textStyle = TextStyle(color = p.onSurface, fontSize = 14.sp),
                            cursorBrush = SolidColor(p.primary),
                            decorationBox = { inner ->
                                Column(Modifier.padding(horizontal = 16.dp, vertical = 11.dp)) {
                                    if (search.isEmpty()) {
                                        Text("🔍 " + stringResource(R.string.search_hint), color = p.onSv, fontSize = 14.sp)
                                    }
                                    inner()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                items(currencyList, key = { it.code }) { cur ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    ) {
                        Text("${cur.flag} ${cur.code}", color = p.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(
                            if (chinese) cur.zh else cur.en,
                            color = p.onSv, fontSize = 11.sp,
                            modifier = Modifier.weight(1f).padding(start = 8.dp),
                        )
                        Switch(
                            checked = cur.code in st.displayCurrencies,
                            onCheckedChange = { vm.toggleDisplayCurrency(cur.code) },
                            colors = SwitchDefaults.colors(checkedTrackColor = p.primary),
                        )
                    }
                }
            }
        }
    }

    // 默认基准货币选择弹窗
    if (showBasePicker) {
        Dialog(onDismissRequest = { showBasePicker = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = p.surface) {
                LazyColumn(Modifier.width(300.dp).height(440.dp).padding(vertical = 8.dp)) {
                    items(Currencies.all, key = { it.code }) { cur ->
                        Text(
                            "${cur.flag} ${cur.code}  ${if (chinese) cur.zh else cur.en}",
                            color = if (cur.code == st.baseCurrency) p.onPc else p.onSurface,
                            fontSize = 15.sp,
                            fontWeight = if (cur.code == st.baseCurrency) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    vm.setDefaultBase(cur.code)
                                    showBasePicker = false
                                }
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }

    // 删除账号确认
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_text)) },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; vm.deleteAccount() }) {
                    Text(stringResource(R.string.confirm), color = p.onErr)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun SectionTitle(res: Int) {
    val p = LocalPalette.current
    Text(
        stringResource(res).uppercase(),
        color = p.onSv, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 22.dp, bottom = 8.dp),
    )
}

@Composable
private fun ThemeButton(mode: String, label: Int, current: String, vm: AppViewModel, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val active = current == mode
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (active) p.primaryC else p.surfaceC,
        modifier = modifier.clickable { vm.setThemeMode(mode) },
    ) {
        Text(
            stringResource(label),
            color = if (active) p.onPc else p.onSv,
            fontSize = 13.sp, fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        )
    }
}
