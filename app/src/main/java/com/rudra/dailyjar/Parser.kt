package com.rudra.dailyjar

object Parser {
    private val amount = Regex("""(?:rs\.?|inr|₹)\s*([\d,]+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
    private val merchant = Regex("""(?:\bto|\bat|@)\s+([A-Za-z0-9 .&_-]{3,24})""", RegexOption.IGNORE_CASE)
    private val money_out = Regex("debited|spent|paid|sent|withdrawn", RegexOption.IGNORE_CASE)

    /** Returns (amount, merchant) for a debit SMS, or null for credits, OTPs and promotions. */
    fun parse(text: String): Pair<Double, String>? {
        if (!money_out.containsMatchIn(text)) return null
        val a = amount.find(text) ?: return null
        val amt = a.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null
        if (amt <= 0) return null
        val note = merchant.find(text)?.groupValues?.get(1)?.trim() ?: "Bank SMS"
        return Pair(amt, note)
    }
}
