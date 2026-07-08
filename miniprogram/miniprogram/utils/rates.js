/**
 * 汇率获取与缓存 —— 策略与网页版一致：
 * 云函数拉取（境外汇率 API 无备案、小程序端无法直连，由云函数中转）；
 * 成功后写本地缓存；启动优先用缓存；全失败保持现有数据。
 */
const { BUILTIN_RATES } = require('./currencies');

const CACHE_KEY = 'cachedRates';

/** 读取启动时应使用的汇率：缓存优先，其次内置 */
function loadInitial() {
  try {
    const cached = wx.getStorageSync(CACHE_KEY);
    if (cached && cached.rates && typeof cached.rates.USD === 'number') {
      return {
        rates: Object.assign({}, BUILTIN_RATES, cached.rates),
        state: 'cached',
        source: cached.source || '',
        ts: cached.ts || 0,
      };
    }
  } catch (e) { /* 缓存损坏则忽略 */ }
  return { rates: Object.assign({}, BUILTIN_RATES), state: 'builtin', source: '', ts: 0 };
}

/** 通过云函数获取实时汇率；成功 resolve {rates, source, ts}，失败 reject */
function fetchLive() {
  return new Promise((resolve, reject) => {
    if (!wx.cloud || !getApp().globalData.cloudReady) {
      reject(new Error('cloud-not-ready'));
      return;
    }
    wx.cloud.callFunction({ name: 'fetchRates' })
      .then((res) => {
        const r = res && res.result;
        if (r && r.ok && r.rates && typeof r.rates.USD === 'number') {
          const ts = Date.now();
          try {
            wx.setStorageSync(CACHE_KEY, { rates: r.rates, source: r.source, ts });
          } catch (e) { /* 存储满等异常忽略 */ }
          resolve({
            rates: Object.assign({}, BUILTIN_RATES, r.rates),
            source: r.source,
            ts,
          });
        } else {
          reject(new Error('bad-result'));
        }
      })
      .catch(reject);
  });
}

module.exports = { loadInitial, fetchLive };
