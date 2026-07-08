/**
 * 计算引擎 —— 语义与网页版 evalExpression / 安卓版 CalcEngine 一致。
 * 小程序禁用 eval/new Function，故使用递归下降解析器。
 */

function round12(v) {
  return parseFloat(v.toPrecision(12));
}

function parseExprString(s) {
  let i = 0;
  function peek() { return i < s.length ? s[i] : null; }
  function parseFactor() {
    let sign = 1;
    while (peek() === '+' || peek() === '-') {
      if (s[i] === '-') sign = -sign;
      i++;
    }
    const start = i;
    while (peek() !== null && (/[0-9.]/.test(s[i]))) i++;
    if (i === start) throw new Error('number expected');
    const n = Number(s.slice(start, i));
    if (Number.isNaN(n)) throw new Error('bad number');
    return sign * n;
  }
  function parseTerm() {
    let v = parseFactor();
    for (;;) {
      if (peek() === '*') { i++; v *= parseFactor(); }
      else if (peek() === '/') { i++; v /= parseFactor(); }
      else return v;
    }
  }
  function parseExpr() {
    let v = parseTerm();
    for (;;) {
      if (peek() === '+') { i++; v += parseTerm(); }
      else if (peek() === '-') { i++; v -= parseTerm(); }
      else return v;
    }
  }
  const v = parseExpr();
  if (i < s.length) throw new Error('unexpected char');
  return v;
}

/** 完整求值；非法表达式或非有限结果返回 null */
function evalExpression(expr) {
  const s = String(expr)
    .replace(/×/g, '*')
    .replace(/÷/g, '/')
    .replace(/−/g, '-')
    .replace(/\s/g, '');
  if (!s || !/^[0-9+\-*/.]+$/.test(s)) return null;
  try {
    const v = parseExprString(s);
    if (typeof v !== 'number' || !isFinite(v)) return null;
    return round12(v);
  } catch (e) {
    return null;
  }
}

/** 实时预览：表达式未输完（如 "5+"）时按已完成部分求值 */
function evalPreview(expr) {
  const full = evalExpression(expr);
  if (full !== null) return full;
  const stripped = String(expr).replace(/[+\-*/.×÷−\s]+$/, '');
  if (!stripped) return null;
  return evalExpression(stripped);
}

/** 与 JS Number#toString 对齐的字符串化 */
function numToStr(v) {
  return String(round12(v));
}

/** 结果显示：千分符分组（与网页版 formatNumber 一致） */
function formatNumber(v) {
  if (v === 0 || v === '' || Number.isNaN(v)) return '0';
  if (Math.abs(v) >= 1e9) return v.toExponential(2);
  const parts = String(v).split('.');
  parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  return parts.join('.');
}

/** 卡片金额：固定两位小数 + 千分符 */
function formatMoney(v) {
  const fixed = v.toFixed(2);
  const parts = fixed.split('.');
  parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  return parts.join('.');
}

module.exports = { evalExpression, evalPreview, numToStr, formatNumber, formatMoney, round12 };
