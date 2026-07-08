/**
 * 本地配置存储 + 可选的云同步（微信云开发数据库，集合 configs，权限"仅创建者可读写"）。
 * 任何页面写配置都走 set()，云同步开启时自动防抖 2 秒上传快照。
 */

function get(key, dflt) {
  try {
    const v = wx.getStorageSync(key);
    return (v === '' || v === undefined || v === null) ? dflt : v;
  } catch (e) {
    return dflt;
  }
}

let timer = null;

function set(key, value) {
  try { wx.setStorageSync(key, value); } catch (e) { /* 忽略 */ }
  if (key !== 'syncOn') scheduleSync();
}

function cloudReady() {
  try {
    return !!(wx.cloud && getApp().globalData.cloudReady);
  } catch (e) {
    return false;
  }
}

function snapshot() {
  return {
    base: get('base', 'CNY'),
    pinned: get('pinned', []),
    displayCurrencies: get('displayCurrencies', null),
    themeMode: get('themeMode', 'auto'),
    updatedAt: Date.now(),
  };
}

function scheduleSync() {
  if (!get('syncOn', false) || !cloudReady()) return;
  if (timer) clearTimeout(timer);
  timer = setTimeout(upload, 2000);
}

function upload(cb) {
  if (!cloudReady()) { if (cb) cb(false); return; }
  const db = wx.cloud.database();
  const data = snapshot();
  db.collection('configs').limit(1).get()
    .then((res) => {
      if (res.data && res.data.length > 0) {
        return db.collection('configs').doc(res.data[0]._id).update({ data });
      }
      return db.collection('configs').add({ data });
    })
    .then(() => { if (cb) cb(true); })
    .catch(() => { if (cb) cb(false); });
}

/** 从云端恢复配置到本地存储；cb(true) 表示有数据并已应用 */
function restore(cb) {
  if (!cloudReady()) { cb(false); return; }
  const db = wx.cloud.database();
  db.collection('configs').limit(1).get()
    .then((res) => {
      if (!res.data || res.data.length === 0) { cb(false); return; }
      const d = res.data[0];
      if (d.base) wx.setStorageSync('base', d.base);
      if (Array.isArray(d.pinned)) wx.setStorageSync('pinned', d.pinned);
      if (Array.isArray(d.displayCurrencies)) wx.setStorageSync('displayCurrencies', d.displayCurrencies);
      if (d.themeMode) wx.setStorageSync('themeMode', d.themeMode);
      cb(true);
    })
    .catch(() => cb(false));
}

module.exports = { get, set, upload, restore, scheduleSync, cloudReady };
