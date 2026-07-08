# BBCalc 微信小程序版

原生小程序（WXML/WXSS/JS），**核心逻辑与网页版 / 安卓版保持一致**：

| 模块 | 说明 |
|---|---|
| `miniprogram/utils/calc.js` | 计算引擎：递归下降解析器（小程序禁用 eval），12 位有效数字精度、未完成表达式实时预览——语义与网页版 `evalExpression`、安卓 `CalcEngine.kt` 完全一致 |
| `miniprogram/utils/currencies.js` | 同一张 40 货币表 + 内置兜底汇率（USD 锚点） |
| `miniprogram/utils/rates.js` + `cloudfunctions/fetchRates` | 汇率管线：云函数中转拉取（境外 API 无备案、小程序端无法直连）→ 本地缓存 → 内置兜底 |
| `pages/index` | 计算器 + 双列汇率卡（点卡片切基准、📌 置顶、搜索） |
| `pages/settings` | 主题（浅色/跟随/深色）、默认基准货币、货币勾选、云同步开关 |

未配置云开发时一切本地功能正常（用缓存/内置汇率），界面会提示"云开发未配置"。

## 上手步骤（你需要做的）

1. **注册小程序**：[mp.weixin.qq.com](https://mp.weixin.qq.com) → 立即注册 → 小程序 → 个人主体（免费，身份证+手机号）。拿到 **AppID**（设置 → 开发设置里查看）
2. **安装微信开发者工具**（[下载](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)，Mac/Windows）
3. **导入项目**：开发者工具 → 导入 → 选择本仓库的 `miniprogram/` 目录 → 填入你的 AppID（`project.config.json` 里的 `touristappid` 会被替换）
4. **开通云开发**：工具栏点"云开发"→ 开通（选按量付费基础环境，轻量使用每月几元）→ 记下环境 ID
5. **部署云函数**：左侧目录树 `cloudfunctions/fetchRates` 右键 → "上传并部署：云端安装依赖"。建议在云开发控制台把该函数**超时时间调到 10 秒**
6. **创建数据库集合**（云同步用，可选）：云开发控制台 → 数据库 → 创建集合 `configs`，权限选"仅创建者可读写"
7. **真机预览**：工具栏"预览"→ 手机微信扫码
8. **发布**：工具栏"上传"→ mp.weixin.qq.com 后台提交审核（类目建议选 **工具 > 效率**；通常 1~2 天出结果）

## 注意事项

- **类目审核**：个人主体不能选金融类目；以"工具"类目提交，若被以"涉及汇率信息服务"打回，可在申诉中说明本程序仅做汇率换算计算、数据来自欧洲央行公开数据、不涉及交易/兑换业务
- **云同步**：基于微信云开发数据库（openid 识别用户，无需登录按钮），与网页版/安卓版的 Google 账号体系不互通
- **汇率数据源**：云函数依次尝试 Frankfurter (ECB) → jsDelivr → fastly 镜像，全失败时小程序端用缓存/内置数据兜底
