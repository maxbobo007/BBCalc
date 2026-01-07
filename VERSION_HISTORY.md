# 汇率计算器 - 版本历史

本文件夹保存了汇率计算器的所有版本，方便回退和对比。

---

## 📦 PC版本

### v1.0.0-stable.html（原始稳定版）
**发布日期：** 2025-01-07
**状态：** ✅ 稳定，推荐使用

**功能：**
- ✅ 基础计算器功能（+、-、×、÷）
- ✅ 40种货币支持
- ✅ 实时汇率换算
- ✅ 3个API源自动切换
- ✅ 离线汇率支持（2025-01）
- ✅ 货币选择和设置
- ✅ API配置功能

**已知问题：**
- 数字没有千分符（例如显示 1000000）
- 底部按钮布局：0按钮占2格，.和=按钮不对齐
- 只支持中文界面

**适用场景：**
- 需要最稳定可靠的版本
- 不需要花哨功能，只要能用

**文件大小：** ~37KB

---

### v1.1.0-with-improvements.html（改进版）
**发布日期：** 2025-01-07
**状态：** ✅ 稳定，推荐使用

**新增功能：**
- ✅ **千分符格式化**：1000000 显示为 1,000,000
- ✅ **00按钮**：快速输入大数，布局更协调（00 | 0 | . | =）
- ✅ **多语言支持**：中文、English、Español、Français、日本語、Deutsch
- ✅ 语言自动保存

**改进：**
- 数字更易读（千分符）
- 底部4按钮完美对齐
- 国际化支持

**已知限制：**
- 未实现"运算符不清空显示"功能（避免语法错误）

**适用场景：**
- 需要更好的用户体验
- 国际用户
- 处理大额数字

**文件大小：** ~40KB

---

### v1.0.0-improved-with-bugs.html（已废弃）
**发布日期：** 2025-01-07
**状态：** ❌ 有严重bug，已废弃

**问题：**
- JavaScript语法错误
- 函数定义不完整
- 点击任何按钮都报错：`append is not defined`

**结论：** 不要使用此版本！

---

## 📱 移动版本

### mobile-v1.0.0.html
**发布日期：** 2025-01-07
**状态：** ✅ 稳定

**特点：**
- iOS风格界面
- 点击货币快速切换
- 移动端优化布局
- 触摸友好

**功能：**
- 基础计算器
- 40种货币
- 实时汇率换算
- API配置

**文件大小：** ~35KB

---

## 🔄 版本对比

| 功能 | v1.0.0 | v1.1.0 | Mobile v1.0.0 |
|------|--------|--------|---------------|
| 基础计算 | ✅ | ✅ | ✅ |
| 货币换算 | ✅ | ✅ | ✅ |
| 千分符 | ❌ | ✅ | ❌ |
| 00按钮 | ❌ | ✅ | ❌ |
| 多语言 | ❌ | ✅ | ❌ |
| 按钮对齐 | ❌ | ✅ | ✅ |
| 适用平台 | PC | PC | Mobile |

---

## 📊 推荐使用

### 如果你要...
- **最稳定的PC版本** → v1.0.0-stable.html
- **最好的PC体验** → v1.1.0-with-improvements.html
- **移动端使用** → mobile-v1.0.0.html

### 构建Mac应用推荐
```bash
# 使用 v1.1.0（功能最完整）
cp v1.1.0-with-improvements.html calculator-pc.html
npm run build:mac-universal
```

---

## 🗂️ 文件结构

```
all-versions/
├── VERSION_HISTORY.md          # 本文件
├── v1.0.0-stable.html          # 原始稳定版
├── v1.1.0-with-improvements.html  # 改进版（推荐）
└── mobile-v1.0.0.html          # 移动版
```

---

## 🔄 如何使用这些版本

### 在浏览器测试
```bash
# 直接双击任何html文件即可在浏览器打开
open v1.1.0-with-improvements.html
```

### 替换到项目中
```bash
# 备份当前版本
cd "/Users/maxzhou/Documents/Claude Code/HLU/currency-calculator-package"
cp calculator-pc.html calculator-pc.backup

# 复制想要的版本
cp ~/Downloads/all-versions/v1.1.0-with-improvements.html calculator-pc.html

# 测试
npm start

# 构建
npm run build:mac-universal
```

### 对比两个版本
```bash
# 使用diff工具对比
diff v1.0.0-stable.html v1.1.0-with-improvements.html

# 或使用可视化diff工具
code --diff v1.0.0-stable.html v1.1.0-with-improvements.html
```

---

## 📝 版本命名规范

格式：`v主版本.次版本.修订版本-描述.html`

示例：
- `v1.0.0-stable.html` - 1.0.0稳定版
- `v1.1.0-with-improvements.html` - 1.1.0带改进的版本
- `v1.2.0-multilang.html` - 1.2.0多语言版本

---

## 🎯 版本选择决策树

```
需要使用计算器？
├─ 是移动端 → mobile-v1.0.0.html
└─ 是PC端
   ├─ 只要能用，最稳定 → v1.0.0-stable.html
   └─ 要最好体验 → v1.1.0-with-improvements.html ⭐
```

---

## 📅 更新日志

### 2025-01-07
- 创建版本历史
- 保存 v1.0.0 原始稳定版
- 保存 v1.1.0 改进版（千分符、00按钮、多语言）
- 保存 mobile v1.0.0 移动版

### 未来计划
- v1.2.0: 运算符保留显示功能
- v1.3.0: 暗黑模式
- v2.0.0: 完全重构，添加更多功能

---

## 💾 备份建议

1. **本地备份**：将此文件夹复制到其他硬盘
2. **云端备份**：上传到iCloud、Google Drive或Dropbox
3. **Git备份**：使用Git + GitHub（最专业）
4. **定期备份**：每次重要更新后备份

---

## 🔗 相关文档

- `GIT_VERSION_CONTROL.md` - Git版本控制完整指南
- `GIT_CHEATSHEET.md` - Git命令速查表
- `setup-git.sh` - Git自动设置脚本
- `MAC_BUILD_GUIDE.md` - Mac应用构建指南
- `DEPLOYMENT_GUIDE.md` - 多平台部署指南

---

**保存好这些版本，永远不用担心找不回来！** 🎉
