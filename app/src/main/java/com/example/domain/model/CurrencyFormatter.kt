package com.example.domain.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object CurrencyFormatter {

    // 1 Taka = 100 Poisha
    const val POISHA_PER_TAKA = 100L

    // 1 Attendance = 1000 Units (e.g., 0.5 = 500, 1.5 = 1500)
    const val ATTENDANCE_SCALE = 1000

    private val BENGALI_DIGITS = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun convertToBengaliDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(BENGALI_DIGITS[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Format poisha into BDT with Taka symbol (৳).
     * Example: 150000L -> "৳ ১,৫০০" (or "৳ 1,500")
     */
    fun formatTaka(poisha: Long, useBengaliDigits: Boolean = true): String {
        val isNegative = poisha < 0
        val absPoisha = kotlin.math.abs(poisha)
        val taka = absPoisha / POISHA_PER_TAKA
        val remPoisha = absPoisha % POISHA_PER_TAKA

        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0", symbols)
        val formattedTaka = df.format(taka)

        val resultStr = if (remPoisha > 0) {
            "$formattedTaka.${String.format(Locale.US, "%02d", remPoisha)}"
        } else {
            formattedTaka
        }

        val prefix = if (isNegative) "-৳ " else "৳ "
        return if (useBengaliDigits) {
            prefix + convertToBengaliDigits(resultStr)
        } else {
            prefix + resultStr
        }
    }

    /**
     * Format attendance units into string like "১.৫ হাজিরা" or "১ হাজিরা"
     */
    fun formatAttendance(units: Int, useBengaliDigits: Boolean = true): String {
        val value = units.toDouble() / ATTENDANCE_SCALE
        val str = if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", value)
        }
        val label = if (useBengaliDigits) "${convertToBengaliDigits(str)} হাজিরা" else "$str হাজিরা"
        return label
    }

    /**
     * Format units to plain number string (e.g. 1500 -> "1.5", 1000 -> "1")
     */
    fun unitsToAttendanceString(units: Int): String {
        val value = units.toDouble() / ATTENDANCE_SCALE
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", value)
        }
    }

    /**
     * Parse attendance text (e.g. "1.5", "1", "0.5") into units (1500, 1000, 500)
     */
    fun attendanceToUnits(text: String): Int {
        val clean = text.trim()
            .replace('০', '0').replace('১', '1').replace('২', '2')
            .replace('৩', '3').replace('৪', '4').replace('৫', '5')
            .replace('৬', '6').replace('৭', '7').replace('৮', '8')
            .replace('৯', '9')
        val parsed = clean.toDoubleOrNull() ?: 0.0
        return (parsed * ATTENDANCE_SCALE).toInt().coerceAtLeast(0)
    }

    /**
     * Parse Taka input text (e.g. "1000", "1250.50") into Poisha (Long)
     */
    fun takaToPoisha(text: String): Long {
        val clean = text.trim()
            .replace(",", "")
            .replace('০', '0').replace('১', '1').replace('২', '2')
            .replace('৩', '3').replace('৪', '4').replace('৫', '5')
            .replace('৬', '6').replace('৭', '7').replace('৮', '8')
            .replace('৯', '9')
        val parts = clean.split(".")
        val takaPart = parts.getOrNull(0)?.toLongOrNull() ?: 0L
        val poishaPart = if (parts.size > 1) {
            val pStr = parts[1].padEnd(2, '0').take(2)
            pStr.toLongOrNull() ?: 0L
        } else {
            0L
        }
        return (takaPart * POISHA_PER_TAKA) + poishaPart
    }

    /**
     * Poisha to editable plain Taka string (e.g. 100000L -> "1000")
     */
    fun poishaToTakaInput(poisha: Long): String {
        val taka = poisha / POISHA_PER_TAKA
        val rem = poisha % POISHA_PER_TAKA
        return if (rem > 0) "$taka.${String.format(Locale.US, "%02d", rem)}" else taka.toString()
    }

    /**
     * Calculates earned amount deterministically:
     * earnedPoisha = (attendanceUnits * rateInPoisha) / ATTENDANCE_SCALE
     */
    fun calculateEarnedPoisha(attendanceUnits: Int, rateInPoisha: Long): Long {
        return (attendanceUnits.toLong() * rateInPoisha) / ATTENDANCE_SCALE
    }

    /**
     * Format ISO date string (YYYY-MM-DD) to friendly Bengali display:
     * e.g. "২০ সেপ্টেম্বর ২০২৬"
     */
    fun formatDateBengali(isoDate: String, useBengaliDigits: Boolean = true): String {
        return try {
            val date = LocalDate.parse(isoDate)
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            if (date == today) {
                return "আজ (${formatFullBengaliDate(date, useBengaliDigits)})"
            } else if (date == yesterday) {
                return "গতকাল (${formatFullBengaliDate(date, useBengaliDigits)})"
            }
            formatFullBengaliDate(date, useBengaliDigits)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatShortDateBengali(isoDate: String, useBengaliDigits: Boolean = true): String {
        return try {
            val date = LocalDate.parse(isoDate)
            val day = date.dayOfMonth.toString()
            val monthNames = arrayOf(
                "জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন",
                "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে"
            )
            val monthName = monthNames[date.monthValue - 1]
            val dayStr = if (useBengaliDigits) convertToBengaliDigits(day) else day
            "$dayStr $monthName"
        } catch (_: Exception) {
            isoDate
        }
    }

    private fun formatFullBengaliDate(date: LocalDate, useBengaliDigits: Boolean): String {
        val day = date.dayOfMonth.toString()
        val year = date.year.toString()
        val monthNames = arrayOf(
            "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
            "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
        )
        val monthName = monthNames[date.monthValue - 1]

        val dayStr = if (useBengaliDigits) convertToBengaliDigits(day) else day
        val yearStr = if (useBengaliDigits) convertToBengaliDigits(year) else year

        return "$dayStr $monthName $yearStr"
    }

    fun todayIso(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
