const calc = require('../../utils/calc');
const cur = require('../../utils/currencies');
const rates = require('../../utils/rates');
const store = require('../../utils/store');

Page({
  data: {
    themeClass: 'auto',
    base: 'CNY',
    expressionText: '100',
    resultText: '100',
    cards: [],
    search: '',
    rateInfo: '',
    updating: false,
  },

  // 非渲染状态
  expression: '100',
  isError: false,
  preview: 100,
  ratesMap: {},
  rateState: 'builtin',
  rateSource: '',
  rateTs: 0,

  onLoad() {
    const init = rates.loadInitial();
    this.ratesMap = init.rates;
    this.rateState = init.state;
    this.rateSource = init.source;
    this.rateTs = init.ts;
    this.refreshAll();
    this.fetchRates();
  },

  onShow() {
    // 从设置页返回时刷新主题/基准货币/显示货币
    this.refreshAll();
  },

  refreshAll() {
    this.setData({
      themeClass: store.get('themeMode', 'auto'),
      base: store.get('base', 'CNY'),
    });
    this.refreshDisplay();
    this.refreshCards();
    this.refreshRateInfo();
  },

  // ── 计算器 ──

  onKey(e) {
    const k = e.currentTarget.dataset.k;
    if (k === 'C') { this.clearCalc(); return; }
    if (k === 'B') { this.backspace(); return; }
    if (k === 'S') { this.toggleSign(); return; }
    if (k === 'P') { this.percent(); return; }
    if (k === '=') { this.calculate(); return; }
    this.append(k);
  },

  append(ch) {
    let e = this.isError ? '' : this.expression; // 出错后直接开始新输入
    this.isError = false;
    e = (e === '0' && ch !== '.') ? ch : e + ch;
    this.setExpression(e);
  },

  backspace() {
    if (this.isError) { this.clearCalc(); return; }
    if (this.expression.length > 0) this.setExpression(this.expression.slice(0, -1));
  },

  clearCalc() {
    this.expression = '';
    this.isError = false;
    this.preview = 0;
    this.refreshDisplay();
    this.refreshCards();
  },

  toggleSign() {
    if (this.preview !== 0 && !this.isError) {
      const v = calc.round12(-this.preview);
      this.expression = calc.numToStr(v);
      this.preview = v;
      this.refreshDisplay();
      this.refreshCards();
    }
  },

  percent() {
    if (this.preview !== 0 && !this.isError) {
      const v = calc.round12(this.preview / 100);
      this.expression = calc.numToStr(v);
      this.preview = v;
      this.refreshDisplay();
      this.refreshCards();
    }
  },

  calculate() {
    const v = calc.evalExpression(this.expression);
    if (v === null) {
      this.isError = true;
      this.expression = '';
      this.preview = 0;
    } else {
      this.isError = false;
      this.expression = calc.numToStr(v);
      this.preview = v;
    }
    this.refreshDisplay();
    this.refreshCards();
  },

  setExpression(e) {
    this.expression = e;
    // 未输完时保留上一个有效值，避免换算列表闪 0（与网页版一致）
    const p = calc.evalPreview(e);
    if (p !== null) this.preview = p;
    this.refreshDisplay();
    this.refreshCards();
  },

  refreshDisplay() {
    this.setData({
      expressionText: this.isError ? '错误' : this.expression,
      resultText: calc.formatNumber(this.preview),
    });
  },

  // ── 货币卡片 ──

  refreshCards() {
    const base = store.get('base', 'CNY');
    const pinned = store.get('pinned', []);
    const display = store.get('displayCurrencies', null);
    const displaySet = Array.isArray(display) ? display : cur.ALL.map((c) => c.code);
    const q = this.data.search.trim().toLowerCase();
    const baseRate = this.ratesMap[base];

    const list = cur.ALL
      .filter((c) => c.code !== base && displaySet.indexOf(c.code) !== -1)
      .filter((c) => !q || c.code.toLowerCase().indexOf(q) !== -1 || c.name.indexOf(q) !== -1);

    const make = (c) => {
      const rate = this.ratesMap[c.code];
      const v = (rate && baseRate && this.preview !== 0 && !this.isError)
        ? (this.preview / baseRate) * rate : 0;
      return {
        code: c.code,
        flag: c.flag,
        name: c.name,
        value: calc.formatMoney(v),
        rawValue: v,
        pinned: pinned.indexOf(c.code) !== -1,
      };
    };

    const cards = list.filter((c) => pinned.indexOf(c.code) !== -1).map(make)
      .concat(list.filter((c) => pinned.indexOf(c.code) === -1).map(make));

    this.setData({ cards, base });
  },

  onSearch(e) {
    this.setData({ search: e.detail.value });
    this.refreshCards();
  },

  /** 点卡片切换基准货币：显示值变为该货币的换算金额（与网页版一致） */
  onCardTap(e) {
    const code = e.currentTarget.dataset.code;
    const card = this.data.cards.find((c) => c.code === code);
    if (!card) return;
    store.set('base', code);
    if (this.preview !== 0 && !this.isError && card.rawValue !== 0) {
      const s = card.rawValue.toFixed(2);
      this.expression = s;
      const p = calc.evalPreview(s);
      if (p !== null) this.preview = p;
    }
    this.setData({ base: code });
    this.refreshDisplay();
    this.refreshCards();
  },

  onPinTap(e) {
    const code = e.currentTarget.dataset.code;
    const pinned = store.get('pinned', []).slice();
    const idx = pinned.indexOf(code);
    if (idx === -1) pinned.push(code); else pinned.splice(idx, 1);
    store.set('pinned', pinned);
    this.refreshCards();
  },

  // ── 汇率 ──

  fetchRates() {
    if (this.data.updating) return;
    this.setData({ updating: true });
    rates.fetchLive()
      .then((r) => {
        this.ratesMap = r.rates;
        this.rateState = 'live';
        this.rateSource = r.source;
        this.rateTs = r.ts;
        this.refreshCards();
      })
      .catch(() => { /* 全失败：保持缓存/内置数据 */ })
      .then(() => {
        this.setData({ updating: false });
        this.refreshRateInfo();
      });
  },

  refreshRateInfo() {
    const stateText = { live: '实时汇率', cached: '缓存汇率', builtin: '离线汇率' }[this.rateState];
    const source = this.rateState === 'builtin' ? '内置数据 (2025-01)' : this.rateSource;
    let time = '';
    if (this.rateTs > 0) {
      const d = new Date(this.rateTs);
      time = (d.getMonth() + 1) + '/' + d.getDate() + ' '
        + String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0');
    }
    let info = [time, stateText, '·', source].filter(Boolean).join(' ');
    if (!store.cloudReady()) info += '（云开发未配置）';
    this.setData({ rateInfo: info });
  },

  openSettings() {
    wx.navigateTo({ url: '/pages/settings/settings' });
  },
});
