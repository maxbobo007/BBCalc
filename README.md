# BBCalc 波波汇率计算器

一个简洁好用的多货币汇率计算器，支持网页版和 Mac 桌面版。

**[🌐 打开网页版](https://maxbobo007.github.io/BBCalc/)**

---

## 功能

- **实时汇率**：自动从多个 API 获取最新汇率，离线时使用内置数据兜底
- **多货币换算**：支持 30+ 种主流货币，输入金额即时显示所有换算结果
- **点击切换基准货币**：点右侧任意货币卡片，直接将其设为基准货币
- **置顶**：📌 按钮将常用货币固定在列表顶部
- **拖拽排序**：自由调整货币显示顺序
- **Google 账号同步**：登录后配置自动保存到云端，多设备共享
- **主题切换**：浅色 / 深色 / 跟随系统
- **多语言**：中文、English、日本語、Español、Français、Deutsch

## 本地运行 / Mac 桌面版

需要先安装 [Node.js](https://nodejs.org)。

```bash
git clone https://github.com/maxbobo007/BBCalc.git
cd BBCalc
npm install
npm start
```

## 技术栈

- 原生 HTML / CSS / JavaScript（无框架）
- [Electron](https://electronjs.org) — Mac 桌面封装
- [Firebase](https://firebase.google.com) — Google 登录 + Firestore 云同步
- GitHub Pages — 网页托管

## License

MIT
