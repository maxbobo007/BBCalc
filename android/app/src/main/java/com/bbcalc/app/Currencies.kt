package com.bbcalc.app

/** 货币数据 —— 与网页版 index.html 中的 currencyFlags / currencyNames / exchangeRates 保持一致 */
data class CurrencyInfo(val code: String, val flag: String, val zh: String, val en: String)

object Currencies {

    val all: List<CurrencyInfo> = listOf(
        CurrencyInfo("USD", "🇺🇸", "美元", "US Dollar"),
        CurrencyInfo("EUR", "🇪🇺", "欧元", "Euro"),
        CurrencyInfo("GBP", "🇬🇧", "英镑", "British Pound"),
        CurrencyInfo("JPY", "🇯🇵", "日元", "Japanese Yen"),
        CurrencyInfo("CNY", "🇨🇳", "人民币", "Chinese Yuan"),
        CurrencyInfo("HKD", "🇭🇰", "港币", "Hong Kong Dollar"),
        CurrencyInfo("AUD", "🇦🇺", "澳元", "Australian Dollar"),
        CurrencyInfo("CAD", "🇨🇦", "加元", "Canadian Dollar"),
        CurrencyInfo("SGD", "🇸🇬", "新币", "Singapore Dollar"),
        CurrencyInfo("KRW", "🇰🇷", "韩元", "South Korean Won"),
        CurrencyInfo("CHF", "🇨🇭", "瑞士法郎", "Swiss Franc"),
        CurrencyInfo("SEK", "🇸🇪", "瑞典克朗", "Swedish Krona"),
        CurrencyInfo("NOK", "🇳🇴", "挪威克朗", "Norwegian Krone"),
        CurrencyInfo("DKK", "🇩🇰", "丹麦克朗", "Danish Krone"),
        CurrencyInfo("NZD", "🇳🇿", "新西兰元", "New Zealand Dollar"),
        CurrencyInfo("MXN", "🇲🇽", "墨西哥比索", "Mexican Peso"),
        CurrencyInfo("BRL", "🇧🇷", "巴西雷亚尔", "Brazilian Real"),
        CurrencyInfo("INR", "🇮🇳", "印度卢比", "Indian Rupee"),
        CurrencyInfo("RUB", "🇷🇺", "俄罗斯卢布", "Russian Ruble"),
        CurrencyInfo("ZAR", "🇿🇦", "南非兰特", "South African Rand"),
        CurrencyInfo("THB", "🇹🇭", "泰铢", "Thai Baht"),
        CurrencyInfo("MYR", "🇲🇾", "马来西亚林吉特", "Malaysian Ringgit"),
        CurrencyInfo("IDR", "🇮🇩", "印尼盾", "Indonesian Rupiah"),
        CurrencyInfo("PHP", "🇵🇭", "菲律宾比索", "Philippine Peso"),
        CurrencyInfo("VND", "🇻🇳", "越南盾", "Vietnamese Dong"),
        CurrencyInfo("TWD", "🇹🇼", "新台币", "Taiwan Dollar"),
        CurrencyInfo("TRY", "🇹🇷", "土耳其里拉", "Turkish Lira"),
        CurrencyInfo("SAR", "🇸🇦", "沙特里亚尔", "Saudi Riyal"),
        CurrencyInfo("AED", "🇦🇪", "阿联酋迪拉姆", "UAE Dirham"),
        CurrencyInfo("PLN", "🇵🇱", "波兰兹罗提", "Polish Zloty"),
        CurrencyInfo("CZK", "🇨🇿", "捷克克朗", "Czech Koruna"),
        CurrencyInfo("HUF", "🇭🇺", "匈牙利福林", "Hungarian Forint"),
        CurrencyInfo("ILS", "🇮🇱", "以色列谢克尔", "Israeli Shekel"),
        CurrencyInfo("CLP", "🇨🇱", "智利比索", "Chilean Peso"),
        CurrencyInfo("PKR", "🇵🇰", "巴基斯坦卢比", "Pakistani Rupee"),
        CurrencyInfo("EGP", "🇪🇬", "埃及镑", "Egyptian Pound"),
        CurrencyInfo("ARS", "🇦🇷", "阿根廷比索", "Argentine Peso"),
        CurrencyInfo("COP", "🇨🇴", "哥伦比亚比索", "Colombian Peso"),
        CurrencyInfo("PEN", "🇵🇪", "秘鲁索尔", "Peruvian Sol"),
        CurrencyInfo("UAH", "🇺🇦", "乌克兰格里夫纳", "Ukrainian Hryvnia"),
    )

    val byCode: Map<String, CurrencyInfo> = all.associateBy { it.code }

    /** 内置兜底汇率（USD 锚点，2025-01），与网页版一致 */
    val builtinRates: Map<String, Double> = mapOf(
        "USD" to 1.0000, "EUR" to 0.9234, "GBP" to 0.7912, "JPY" to 149.32,
        "CNY" to 7.2445, "HKD" to 7.8095, "AUD" to 1.5486, "CAD" to 1.3891,
        "SGD" to 1.3502, "KRW" to 1338.45, "CHF" to 0.8812, "SEK" to 10.4823,
        "NOK" to 10.7956, "DKK" to 6.8534, "NZD" to 1.6523, "MXN" to 17.1234,
        "BRL" to 5.0923, "INR" to 83.1567, "RUB" to 92.3456, "ZAR" to 18.5632,
        "THB" to 35.4521, "MYR" to 4.6432, "IDR" to 15742.34, "PHP" to 56.7834,
        "VND" to 24487.56, "TWD" to 31.4523, "TRY" to 32.7845, "SAR" to 3.7512,
        "AED" to 3.6732, "PLN" to 4.0523, "CZK" to 23.1856, "HUF" to 359.87,
        "ILS" to 3.6523, "CLP" to 948.23, "PKR" to 277.89, "EGP" to 48.4523,
        "ARS" to 849.34, "COP" to 4097.56, "PEN" to 3.7423, "UAH" to 41.1834,
    )

    fun name(code: String, chinese: Boolean): String =
        byCode[code]?.let { if (chinese) it.zh else it.en } ?: code
}
