# BBCalc Android（原生版）

Kotlin + Jetpack Compose 原生实现，**核心逻辑与网页版 `index.html` 保持一致**：

| 模块 | 对应网页版逻辑 |
|---|---|
| `CalcEngine.kt` | `evalExpression`：仅允许数字与四则运算、12 位有效数字精度修正、未完成表达式实时预览 |
| `Currencies.kt` | `currencyFlags` / `currencyNames` / 内置兜底汇率表（USD 锚点，2025-01） |
| `AppViewModel.kt` | 汇率获取（currencyapi CDN → frankfurter，8 秒超时）、成功后缓存、启动优先用缓存、点卡片切换基准货币、置顶 |
| `MainActivity.kt` | UI：与网页移动端一致的双列汇率卡 + 计算器布局、MD3 深浅色配色 |

v1（1.8.0）范围：计算器、双列汇率卡、切换基准货币、置顶、搜索、深浅色跟随系统、中/英文跟随系统语言、离线可用。暂未实现：Google 云同步、拖拽排序、多语言全集（这些请用网页版/PWA）。

> 此前的 TWA（网页壳）版本在 git 历史中（v1.7.0 之前的 android/ 目录），需要时可找回。原生版沿用同一包名与签名，可直接覆盖升级。

## 构建

- 自动：GitHub Actions（`.github/workflows/android.yml`）在 `android/` 有改动合并到 main 时自动构建，APK 挂在 [android-latest release](https://github.com/maxbobo007/BBCalc/releases/tag/android-latest)；也可在 Actions 页手动触发
- 本地：装好 Android SDK 后 `cd android && gradle assembleRelease`

## 签名密钥说明（重要）

`demo.keystore`（密码 `bbcalc-demo`）**随仓库公开**，仅用于侧载分发的演示场景。上架 Google Play 前必须：

1. 本地 `keytool -genkeypair` 生成新的私有密钥（妥善保存，Play 更新永远需要它）
2. base64 后存入 GitHub Secrets，workflow 里解码使用，删除 demo.keystore
