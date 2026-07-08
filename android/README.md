# BBCalc Android（TWA）

用 [Trusted Web Activity](https://developer.chrome.com/docs/android/trusted-web-activity/) 把 PWA 网页版打包成安卓 App：APK 只是一层壳，实际运行的是 https://maxbobo007.github.io/BBCalc/ （跑在系统 Chrome 引擎里）。**网页更新 = App 内容自动更新，无需重新发版。**

## 构建

- 自动：GitHub Actions（`.github/workflows/android.yml`）在 `android/` 有改动合并到 main 时自动构建，APK 挂在 [android-latest release](https://github.com/maxbobo007/BBCalc/releases/tag/android-latest)；也可在 Actions 页手动触发
- 本地（可选）：装好 Android SDK 后 `cd android && gradle assembleRelease`

## 签名密钥说明（重要）

`demo.keystore`（密码 `bbcalc-demo`）**随仓库公开**，仅用于侧载分发的演示场景。含义：任何人都能用它签出同签名的 APK。做着玩没问题，但**上架 Google Play 前必须**：

1. 本地 `keytool -genkeypair` 生成新的私有密钥（妥善保存，Play 更新永远需要它）
2. base64 后存入 GitHub Secrets，workflow 里解码使用，删除 demo.keystore
3. 更新 `.well-known/assetlinks.json` 中的证书指纹

## 全屏模式（去掉浏览器地址栏）

TWA 需要通过 Digital Asset Links 验证"这个 App 和这个网站是同一个主人"，验证文件必须放在**站点源（origin）根路径**：

```
https://maxbobo007.github.io/.well-known/assetlinks.json
```

注意这是 `maxbobo007.github.io` 用户站点仓库的根目录，**不是** BBCalc 仓库（`/BBCalc/.well-known/` 不生效）。文件内容（demo 密钥的指纹）：

```json
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "com.bbcalc.app",
    "sha256_cert_fingerprints": [
      "83:17:CE:DB:5E:D0:8D:73:DA:A3:91:A2:CF:54:59:4E:D2:C7:9E:30:2E:37:BB:D5:4F:4D:C6:16:BB:28:65:CD"
    ]
  }
}]
```

未配置时 App 也能正常使用，只是顶部会显示 Chrome 的地址栏。
