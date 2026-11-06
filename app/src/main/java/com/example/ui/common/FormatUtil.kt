package com.example.ui.common

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object FormatUtil {
    fun formatRupiah(amount: Long, formatType: String = "RP_NOSPACE", isPrivacy: Boolean = false): String {
        if (isPrivacy) return "Rp ••••••••"
        val isNegative = amount < 0
        val absAmount = kotlin.math.abs(amount)
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        val formattedNumber = formatter.format(absAmount)

        val prefix = when (formatType) {
            "RP_SPACE" -> "Rp "
            "IDR" -> "IDR "
            else -> "Rp" // RP_NOSPACE
        }

        return if (isNegative) "-$prefix$formattedNumber" else "$prefix$formattedNumber"
    }

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
        return sdf.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yy", Locale.forLanguageTag("id-ID"))
        return sdf.format(Date(millis))
    }

    fun formatMonthYear(millis: Long): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
        return sdf.format(Date(millis))
    }

    fun calculateMonthlySavings(targetAmount: Long, currentBalance: Long, targetDateMillis: Long): Long {
        val shortage = maxOf(0L, targetAmount - currentBalance)
        if (shortage <= 0L) return 0L
        val diffMillis = targetDateMillis - System.currentTimeMillis()
        val oneMonthMillis = 30L * 24 * 60 * 60 * 1000
        val remainingMonths = maxOf(1L, diffMillis / oneMonthMillis)
        return shortage / remainingMonths
    }

    fun getDaysRemaining(targetDateMillis: Long): Long {
        val diffMillis = targetDateMillis - System.currentTimeMillis()
        return maxOf(0L, diffMillis / (24L * 60 * 60 * 1000))
    }
}
