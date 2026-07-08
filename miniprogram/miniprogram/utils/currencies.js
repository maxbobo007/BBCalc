/** 货币数据 —— 与网页版 index.html / 安卓版 Currencies.kt 保持一致 */

const ALL = [
  { code: 'USD', flag: '🇺🇸', name: '美元' },
  { code: 'EUR', flag: '🇪🇺', name: '欧元' },
  { code: 'GBP', flag: '🇬🇧', name: '英镑' },
  { code: 'JPY', flag: '🇯🇵', name: '日元' },
  { code: 'CNY', flag: '🇨🇳', name: '人民币' },
  { code: 'HKD', flag: '🇭🇰', name: '港币' },
  { code: 'AUD', flag: '🇦🇺', name: '澳元' },
  { code: 'CAD', flag: '🇨🇦', name: '加元' },
  { code: 'SGD', flag: '🇸🇬', name: '新币' },
  { code: 'KRW', flag: '🇰🇷', name: '韩元' },
  { code: 'CHF', flag: '🇨🇭', name: '瑞士法郎' },
  { code: 'SEK', flag: '🇸🇪', name: '瑞典克朗' },
  { code: 'NOK', flag: '🇳🇴', name: '挪威克朗' },
  { code: 'DKK', flag: '🇩🇰', name: '丹麦克朗' },
  { code: 'NZD', flag: '🇳🇿', name: '新西兰元' },
  { code: 'MXN', flag: '🇲🇽', name: '墨西哥比索' },
  { code: 'BRL', flag: '🇧🇷', name: '巴西雷亚尔' },
  { code: 'INR', flag: '🇮🇳', name: '印度卢比' },
  { code: 'RUB', flag: '🇷🇺', name: '俄罗斯卢布' },
  { code: 'ZAR', flag: '🇿🇦', name: '南非兰特' },
  { code: 'THB', flag: '🇹🇭', name: '泰铢' },
  { code: 'MYR', flag: '🇲🇾', name: '马来西亚林吉特' },
  { code: 'IDR', flag: '🇮🇩', name: '印尼盾' },
  { code: 'PHP', flag: '🇵🇭', name: '菲律宾比索' },
  { code: 'VND', flag: '🇻🇳', name: '越南盾' },
  { code: 'TWD', flag: '🇹🇼', name: '新台币' },
  { code: 'TRY', flag: '🇹🇷', name: '土耳其里拉' },
  { code: 'SAR', flag: '🇸🇦', name: '沙特里亚尔' },
  { code: 'AED', flag: '🇦🇪', name: '阿联酋迪拉姆' },
  { code: 'PLN', flag: '🇵🇱', name: '波兰兹罗提' },
  { code: 'CZK', flag: '🇨🇿', name: '捷克克朗' },
  { code: 'HUF', flag: '🇭🇺', name: '匈牙利福林' },
  { code: 'ILS', flag: '🇮🇱', name: '以色列谢克尔' },
  { code: 'CLP', flag: '🇨🇱', name: '智利比索' },
  { code: 'PKR', flag: '🇵🇰', name: '巴基斯坦卢比' },
  { code: 'EGP', flag: '🇪🇬', name: '埃及镑' },
  { code: 'ARS', flag: '🇦🇷', name: '阿根廷比索' },
  { code: 'COP', flag: '🇨🇴', name: '哥伦比亚比索' },
  { code: 'PEN', flag: '🇵🇪', name: '秘鲁索尔' },
  { code: 'UAH', flag: '🇺🇦', name: '乌克兰格里夫纳' },
];

const BY_CODE = {};
ALL.forEach((c) => { BY_CODE[c.code] = c; });

/** 内置兜底汇率（USD 锚点，2025-01），与网页版一致 */
const BUILTIN_RATES = {
  USD: 1.0000, EUR: 0.9234, GBP: 0.7912, JPY: 149.32, CNY: 7.2445, HKD: 7.8095,
  AUD: 1.5486, CAD: 1.3891, SGD: 1.3502, KRW: 1338.45, CHF: 0.8812, SEK: 10.4823,
  NOK: 10.7956, DKK: 6.8534, NZD: 1.6523, MXN: 17.1234, BRL: 5.0923, INR: 83.1567,
  RUB: 92.3456, ZAR: 18.5632, THB: 35.4521, MYR: 4.6432, IDR: 15742.34, PHP: 56.7834,
  VND: 24487.56, TWD: 31.4523, TRY: 32.7845, SAR: 3.7512, AED: 3.6732, PLN: 4.0523,
  CZK: 23.1856, HUF: 359.87, ILS: 3.6523, CLP: 948.23, PKR: 277.89, EGP: 48.4523,
  ARS: 849.34, COP: 4097.56, PEN: 3.7423, UAH: 41.1834,
};

module.exports = { ALL, BY_CODE, BUILTIN_RATES };
