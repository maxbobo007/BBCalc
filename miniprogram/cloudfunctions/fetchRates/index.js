/**
 * 云函数：拉取实时汇率（USD 锚点）
 * 小程序端只能请求已备案域名，境外汇率 API 由本函数在服务端中转。
 * 依次尝试多个数据源（兼顾国内网络可达性），任一成功即返回。
 * 建议在云开发控制台将本函数超时设置为 10 秒以上。
 */
const https = require('https');

const SOURCES = [
  {
    name: 'Frankfurter (ECB)',
    url: 'https://api.frankfurter.app/latest?from=USD',
    pick: (data) => data.rates,
  },
  {
    name: 'Currency API (jsDelivr)',
    url: 'https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.json',
    pick: (data) => data.usd,
  },
  {
    name: 'Currency API (fastly)',
    url: 'https://fastly.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.json',
    pick: (data) => data.usd,
  },
];

function fetchJson(url, timeoutMs) {
  return new Promise((resolve, reject) => {
    const req = https.get(url, { timeout: timeoutMs }, (res) => {
      if (res.statusCode !== 200) {
        res.resume();
        reject(new Error('HTTP ' + res.statusCode));
        return;
      }
      let body = '';
      res.setEncoding('utf8');
      res.on('data', (chunk) => { body += chunk; });
      res.on('end', () => {
        try { resolve(JSON.parse(body)); } catch (e) { reject(e); }
      });
    });
    req.on('timeout', () => { req.destroy(new Error('timeout')); });
    req.on('error', reject);
  });
}

exports.main = async () => {
  for (const src of SOURCES) {
    try {
      const data = await fetchJson(src.url, 4000);
      const raw = src.pick(data);
      if (!raw) continue;
      const rates = {};
      for (const k of Object.keys(raw)) {
        const v = Number(raw[k]);
        if (isFinite(v)) rates[k.toUpperCase()] = v;
      }
      if (Object.keys(rates).length > 10) {
        rates.USD = 1;
        return { ok: true, rates, source: src.name };
      }
    } catch (e) {
      // 尝试下一个数据源
    }
  }
  return { ok: false };
};
