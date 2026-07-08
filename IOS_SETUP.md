# BBCalc iOS App Store 上架路线

网页版（PWA）已经完备，本文档记录把 BBCalc 作为独立 App 上架 iOS App Store 的完整路径、当前进度和待办。

## 总览

| 阶段 | 内容 | 状态 |
|---|---|---|
| ① 合规地基 | 隐私政策页、App 内删除账号 | ✅ v1.7.0 已完成 |
| ② 开发者资格 | Apple Developer Program 注册（$99/年）+ Mac/Xcode | ⬜ 需本人操作 |
| ③ 原生封装 | Capacitor 打包 + 登录适配（Sign in with Apple） | ⬜ 待 ② 完成 |
| ④ 差异化与提审 | iOS 桌面小组件、商店素材、TestFlight、提审 | ⬜ 待 ③ 完成 |

## 已完成（阶段①）

- `privacy.html` — 中英双语隐私政策，线上地址：https://maxbobo007.github.io/BBCalc/privacy.html（App Store Connect 提审时填这个 URL）
- App 内删除账号 — 设置 → 账号同步 → 删除账号（Apple 审核指南 5.1.1(v)：提供账号创建就必须提供账号删除）
- `capacitor.config.json` — Capacitor 基础配置（appId 沿用 Electron 的 `com.bbcalc.app`）

## 阶段②：你需要做的（只能本人操作）

1. 注册 [Apple Developer Program](https://developer.apple.com/programs/enroll/)：用开了双重认证的 Apple ID，个人开发者选 "Individual"，$99/年
2. Mac 上安装 Xcode（App Store 免费，体积很大，提前下）
3. 决定上架地区：**建议首发美区/港区等非中国区**——2023 年起中国区 App Store 要求 App 备案（ICP），个人开发者基本无法完成；国内用户用外区 Apple ID 依然可以下载

## 阶段③：原生封装（代码我来写，构建在你 Mac 上）

```bash
# 在 Mac 上、仓库根目录执行
npm install @capacitor/core @capacitor/cli @capacitor/ios
mkdir -p www && cp index.html privacy.html manifest.webmanifest sw.js www/ && cp -r icons www/
npx cap add ios
npx cap sync ios
npx cap open ios   # 打开 Xcode，选择你的开发者证书，即可真机运行
```

已知需要处理的技术点（届时我来改代码）：

- **Google 登录在 WKWebView 里不可用**（Google 禁止内嵌 WebView OAuth），需换用 `@capacitor-firebase/authentication` 插件走原生登录
- **必须加 Sign in with Apple**：Apple 强制要求——提供第三方登录（Google）的 App 必须同时提供 Apple 登录（Firebase 控制台开启 Apple provider + Xcode 加 capability）
- Service Worker 在 Capacitor 本地环境不需要，注册代码已做协议判断自动跳过
- 状态栏/安全区已通过 `viewport-fit=cover` + `env(safe-area-inset-*)` 适配

## 阶段④：差异化与提审

- **iOS 桌面小组件**（SwiftUI WidgetKit）：展示置顶货币的实时汇率——这是过审核指南 4.2（最低功能要求，拒绝纯网页套壳）的关键原生能力，也是同类头部 App 的标配
- 商店素材：6.7"/6.5"/5.5" 截图、App 描述（中英）、关键词（汇率/汇率计算器/currency converter…）、分类选 Finance 或 Utilities
- App 隐私标签申报：收集"标识符（用户 ID）+ 联系信息（邮箱）"，用途"App 功能"，不用于跟踪
- TestFlight 内测 → 提交审核（首次审核通常 1~3 天）

## 定位一句话（商店副标题备选）

> 不做汇款生意的极简汇率：40+ 货币同屏换算，无广告、无推销、可离线。
