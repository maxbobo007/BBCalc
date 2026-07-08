# BBCalc Android（原生版）

Kotlin + Jetpack Compose 原生实现，**核心逻辑与网页版 `index.html` 保持一致**：

| 模块 | 对应网页版逻辑 |
|---|---|
| `CalcEngine.kt` | `evalExpression`：仅允许数字与四则运算、12 位有效数字精度修正、未完成表达式实时预览 |
| `Currencies.kt` | `currencyFlags` / `currencyNames` / 内置兜底汇率表（USD 锚点，2025-01） |
| `AppViewModel.kt` | 汇率获取（currencyapi CDN → frankfurter，8 秒超时）、成功后缓存、启动优先用缓存、点卡片切换基准货币、置顶、Google 登录 + Firestore 云同步 |
| `MainActivity.kt` / `SettingsScreen.kt` | UI：双列汇率卡 + 计算器 + 设置页（主题/默认基准货币/货币勾选/账号），MD3 深浅色配色 |

云同步与网页版**共用同一 Firestore 文档**（`users/{uid}/config/settings`），merge 写入 `pinnedCurrencies` / `displayCurrencies` / `defaultCurrency` / `themeMode` 字段，不覆盖网页版独有的 `currencyOrder` / `appLanguage`——网页和安卓登录同一 Google 账号即可互通配置。

## 启用 Google 登录（需要项目所有者操作一次）

代码已就绪，但登录按钮生效需要 `google-services.json`（Firebase 给安卓 App 的"身份证"）：

1. 打开 [Firebase 控制台](https://console.firebase.google.com/) → 项目 **bbcalc-ad997** → ⚙️ 项目设置 → 你的应用 → **添加应用** → 选 Android
2. 填写：
   - 包名：`com.bbcalc.app`
   - SHA-1 证书指纹：`7E:8C:9E:14:1C:0B:CA:77:0E:9C:B6:90:64:EA:16:E9:12:7A:94:F2`（demo.keystore 的指纹；换签名密钥后需要更新）
3. 下载生成的 `google-services.json`，放到仓库 `android/app/` 目录提交推送 → CI 自动重新打包，登录功能即激活

未配置时 App 一切正常，仅设置页的登录按钮会提示"登录未配置"。

## 构建

- 自动：GitHub Actions（`.github/workflows/android.yml`）在 `android/` 有改动合并到 main 时自动构建，APK 挂在 [android-latest release](https://github.com/maxbobo007/BBCalc/releases/tag/android-latest)；也可在 Actions 页手动触发
- 本地：装好 Android SDK 后 `cd android && gradle assembleRelease`

## 签名密钥说明（重要）

`demo.keystore`（密码 `bbcalc-demo`）**随仓库公开**，仅用于侧载分发的演示场景。上架 Google Play 前必须：

1. 本地 `keytool -genkeypair` 生成新的私有密钥（妥善保存，Play 更新永远需要它）
2. base64 后存入 GitHub Secrets，workflow 里解码使用，删除 demo.keystore
3. 同步更新 Firebase 控制台里的 SHA-1 指纹

> 更早的 TWA（网页壳）版本在 git 历史中（v1.8.0 之前），需要时可找回。
