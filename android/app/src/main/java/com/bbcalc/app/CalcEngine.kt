package com.bbcalc.app

import java.math.BigDecimal
import java.math.MathContext
import java.util.Locale
import kotlin.math.abs

/**
 * 计算引擎 —— 语义与网页版 evalExpression 保持一致：
 * 仅允许数字与 + - * / .，结果按 12 位有效数字修正浮点误差（0.1+0.2 = 0.3）
 */
object CalcEngine {

    fun round12(v: Double): Double =
        try { BigDecimal(v).round(MathContext(12)).toDouble() } catch (e: Exception) { v }

    /** 完整求值；非法表达式或非有限结果返回 null */
    fun eval(expr: String): Double? {
        val s = expr
            .replace('×', '*')
            .replace('÷', '/')
            .replace('−', '-')
            .replace(" ", "")
        if (s.isEmpty() || !s.all { it.isDigit() || it in "+-*/." }) return null
        return try {
            val p = Parser(s)
            val v = p.parseExpr()
            if (!p.atEnd() || !v.isFinite()) null else round12(v)
        } catch (e: Exception) {
            null
        }
    }

    /** 实时预览：表达式未输完（如 "5+"）时按已完成部分求值 */
    fun evalPreview(expr: String): Double? {
        eval(expr)?.let { return it }
        val stripped = expr.trimEnd { it in "+-*/.×÷− " }
        if (stripped.isEmpty()) return null
        return eval(stripped)
    }

    /** 与 JS Number#toString 对齐：整数不带小数点 */
    fun numToStr(v: Double): String {
        val l = v.toLong()
        if (v == l.toDouble() && abs(v) < 1e15) return l.toString()
        return String.format(Locale.US, "%.10f", v).trimEnd('0').trimEnd('.')
    }

    /** 结果显示：千分符分组，与网页版 formatNumber 一致 */
    fun formatNumber(v: Double): String {
        if (v == 0.0) return "0"
        if (abs(v) >= 1e9) return String.format(Locale.US, "%.2e", v)
        val plain = numToStr(v)
        val neg = plain.startsWith("-")
        val body = if (neg) plain.substring(1) else plain
        val parts = body.split(".")
        val grouped = parts[0].reversed().chunked(3).joinToString(",").reversed()
        val out = if (parts.size > 1) grouped + "." + parts[1] else grouped
        return if (neg) "-$out" else out
    }

    /** 递归下降解析：expr := term (('+'|'-') term)* ; term := factor (('*'|'/') factor)* */
    private class Parser(private val s: String) {
        private var i = 0
        fun atEnd() = i >= s.length
        private fun peek(): Char? = if (i < s.length) s[i] else null

        fun parseExpr(): Double {
            var v = parseTerm()
            while (true) {
                when (peek()) {
                    '+' -> { i++; v += parseTerm() }
                    '-' -> { i++; v -= parseTerm() }
                    else -> return v
                }
            }
        }

        private fun parseTerm(): Double {
            var v = parseFactor()
            while (true) {
                when (peek()) {
                    '*' -> { i++; v *= parseFactor() }
                    '/' -> { i++; v /= parseFactor() }
                    else -> return v
                }
            }
        }

        private fun parseFactor(): Double {
            var sign = 1.0
            while (peek() == '+' || peek() == '-') {
                if (s[i] == '-') sign = -sign
                i++
            }
            val start = i
            while (peek()?.let { it.isDigit() || it == '.' } == true) i++
            if (i == start) throw IllegalArgumentException("number expected")
            return sign * s.substring(start, i).toDouble()
        }
    }
}
