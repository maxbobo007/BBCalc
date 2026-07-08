const cur = require('../../utils/currencies');
const store = require('../../utils/store');

Page({
  data: {
    themeClass: 'auto',
    themeMode: 'auto',
    base: 'CNY',
    baseLabel: '',
    basePickerRange: [],
    search: '',
    items: [],       // 货币开关列表
    syncOn: false,
    cloudReady: false,
  },

  onLoad() {
    this.setData({
      basePickerRange: cur.ALL.map((c) => c.flag + ' ' + c.code + ' ' + c.name),
      cloudReady: store.cloudReady(),
      syncOn: !!store.get('syncOn', false),
    });
    this.refresh();
  },

  refresh() {
    const themeMode = store.get('themeMode', 'auto');
    const base = store.get('base', 'CNY');
    const info = cur.BY_CODE[base];
    const display = store.get('displayCurrencies', null);
    const displaySet = Array.isArray(display) ? display : cur.ALL.map((c) => c.code);
    const q = this.data.search.trim().toLowerCase();

    const items = cur.ALL
      .filter((c) => !q || c.code.toLowerCase().indexOf(q) !== -1 || c.name.indexOf(q) !== -1)
      .map((c) => ({
        code: c.code,
        flag: c.flag,
        name: c.name,
        on: displaySet.indexOf(c.code) !== -1,
      }));

    this.setData({
      themeClass: themeMode,
      themeMode,
      base,
      baseLabel: (info ? info.flag + ' ' : '') + base + '  ' + (info ? info.name : ''),
      items,
    });
  },

  onTheme(e) {
    const mode = e.currentTarget.dataset.mode;
    store.set('themeMode', mode);
    this.refresh();
  },

  onBasePick(e) {
    const idx = Number(e.detail.value);
    const code = cur.ALL[idx].code;
    store.set('base', code);
    this.refresh();
  },

  onSearch(e) {
    this.setData({ search: e.detail.value });
    this.refresh();
  },

  onToggleCurrency(e) {
    const code = e.currentTarget.dataset.code;
    const display = store.get('displayCurrencies', null);
    const set = Array.isArray(display) ? display.slice() : cur.ALL.map((c) => c.code);
    const idx = set.indexOf(code);
    if (idx === -1) set.push(code); else set.splice(idx, 1);
    store.set('displayCurrencies', set);
    this.refresh();
  },

  onToggleSync(e) {
    const on = e.detail.value;
    store.set('syncOn', on);
    this.setData({ syncOn: on });
    if (!on) return;
    if (!store.cloudReady()) {
      wx.showToast({ title: '云开发未配置', icon: 'none' });
      return;
    }
    // 开启时：云端有配置则恢复，否则上传本地
    store.restore((restored) => {
      if (restored) {
        this.refresh();
        wx.showToast({ title: '已从云端恢复', icon: 'none' });
      } else {
        store.upload((ok) => {
          wx.showToast({ title: ok ? '已上传本地配置' : '同步失败', icon: 'none' });
        });
      }
    });
  },
});
