const { app, BrowserWindow, Menu, shell, nativeTheme } = require('electron');
const path = require('path');

let mainWindow;

// 设置应用名称
app.name = '汇率计算器';

function createWindow() {
  // 创建浏览器窗口
  mainWindow = new BrowserWindow({
    width: 1400,
    height: 900,
    minWidth: 1200,
    minHeight: 700,
    backgroundColor: '#1a1a2e',
    titleBarStyle: 'hiddenInset', // Mac原生标题栏样式
    trafficLightPosition: { x: 20, y: 20 }, // 红绿灯位置
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      enableRemoteModule: false,
      webSecurity: true
    },
    show: false // 先不显示，等加载完成
  });

  // 加载应用
  mainWindow.loadFile('calculator-pc.html');

  // 窗口准备好后显示（避免白屏）
  mainWindow.once('ready-to-show', () => {
    mainWindow.show();
  });

  // 打开外部链接用系统浏览器
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url);
    return { action: 'deny' };
  });

  // 窗口关闭
  mainWindow.on('closed', () => {
    mainWindow = null;
  });

  // 创建应用菜单
  createMenu();
}

function createMenu() {
  const template = [
    // App菜单（Mac专有）
    {
      label: app.name,
      submenu: [
        {
          label: `关于 ${app.name}`,
          role: 'about'
        },
        { type: 'separator' },
        {
          label: '偏好设置...',
          accelerator: 'Cmd+,',
          click: () => {
            // 可以打开设置页面
            mainWindow.webContents.executeJavaScript('openSettings()');
          }
        },
        { type: 'separator' },
        {
          label: '服务',
          role: 'services'
        },
        { type: 'separator' },
        {
          label: `隐藏 ${app.name}`,
          accelerator: 'Cmd+H',
          role: 'hide'
        },
        {
          label: '隐藏其他',
          accelerator: 'Cmd+Alt+H',
          role: 'hideOthers'
        },
        {
          label: '显示全部',
          role: 'unhide'
        },
        { type: 'separator' },
        {
          label: '退出',
          accelerator: 'Cmd+Q',
          role: 'quit'
        }
      ]
    },
    // 文件菜单
    {
      label: '文件',
      submenu: [
        {
          label: '新建窗口',
          accelerator: 'Cmd+N',
          click: () => {
            createWindow();
          }
        },
        { type: 'separator' },
        {
          label: '关闭窗口',
          accelerator: 'Cmd+W',
          role: 'close'
        }
      ]
    },
    // 编辑菜单
    {
      label: '编辑',
      submenu: [
        { label: '撤销', accelerator: 'Cmd+Z', role: 'undo' },
        { label: '重做', accelerator: 'Shift+Cmd+Z', role: 'redo' },
        { type: 'separator' },
        { label: '剪切', accelerator: 'Cmd+X', role: 'cut' },
        { label: '复制', accelerator: 'Cmd+C', role: 'copy' },
        { label: '粘贴', accelerator: 'Cmd+V', role: 'paste' },
        { label: '全选', accelerator: 'Cmd+A', role: 'selectAll' }
      ]
    },
    // 视图菜单
    {
      label: '视图',
      submenu: [
        {
          label: '刷新汇率',
          accelerator: 'Cmd+R',
          click: () => {
            mainWindow.webContents.executeJavaScript('fetchRates()');
          }
        },
        {
          label: '强制刷新',
          accelerator: 'Cmd+Shift+R',
          click: () => {
            mainWindow.reload();
          }
        },
        { type: 'separator' },
        {
          label: '实际大小',
          accelerator: 'Cmd+0',
          click: () => {
            mainWindow.webContents.setZoomLevel(0);
          }
        },
        {
          label: '放大',
          accelerator: 'Cmd+Plus',
          click: () => {
            const currentZoom = mainWindow.webContents.getZoomLevel();
            mainWindow.webContents.setZoomLevel(currentZoom + 0.5);
          }
        },
        {
          label: '缩小',
          accelerator: 'Cmd+-',
          click: () => {
            const currentZoom = mainWindow.webContents.getZoomLevel();
            mainWindow.webContents.setZoomLevel(currentZoom - 0.5);
          }
        },
        { type: 'separator' },
        {
          label: '全屏',
          accelerator: 'Ctrl+Cmd+F',
          role: 'togglefullscreen'
        },
        { type: 'separator' },
        {
          label: '开发者工具',
          accelerator: 'Alt+Cmd+I',
          click: () => {
            mainWindow.webContents.toggleDevTools();
          }
        }
      ]
    },
    // 窗口菜单
    {
      label: '窗口',
      submenu: [
        { label: '最小化', accelerator: 'Cmd+M', role: 'minimize' },
        { label: '缩放', role: 'zoom' },
        { type: 'separator' },
        { label: '前置全部窗口', role: 'front' }
      ]
    },
    // 帮助菜单
    {
      label: '帮助',
      submenu: [
        {
          label: '使用指南',
          click: () => {
            shell.openExternal('https://github.com/yourusername/calculator/wiki');
          }
        },
        {
          label: '报告问题',
          click: () => {
            shell.openExternal('https://github.com/yourusername/calculator/issues');
          }
        },
        { type: 'separator' },
        {
          label: '查看源代码',
          click: () => {
            shell.openExternal('https://github.com/yourusername/calculator');
          }
        }
      ]
    }
  ];

  const menu = Menu.buildFromTemplate(template);
  Menu.setApplicationMenu(menu);
}

// 应用启动
app.whenReady().then(() => {
  createWindow();

  // Mac特性：点击Dock图标时重新创建窗口
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });
});

// 所有窗口关闭时退出（Windows/Linux）
// Mac上通常不退出应用
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});