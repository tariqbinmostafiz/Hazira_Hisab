package com.shifat.hajirahisab.domain.model

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object CurrencyFormatter {

    const val POISHA_PER_TAKA = 100L
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
     * Formats poisha into Taka string (e.g. 500000L -> "৳৫,০০০" or "-৳১,২০০")
     */
    fun formatTaka(
        poisha: Long,
        useBengaliDigits: Boolean = true,
        showDecimals: Boolean = false
    ): String {
        val isNegative = poisha < 0
        val absPoisha = kotlin.math.abs(poisha)
        val taka = absPoisha / POISHA_PER_TAKA
        val rem = absPoisha % POISHA_PER_TAKA

        val rawTakaStr = if (showDecimals || rem > 0) {
            String.format(Locale.US, "%,d.%02d", taka, rem)
        } else {
            String.format(Locale.US, "%,d", taka)
        }

        val formattedDigits = if (useBengaliDigits) {
            convertToBengaliDigits(rawTakaStr)
        } else {
            rawTakaStr
        }

        return if (isNegative) "-৳$formattedDigits" else "৳$formattedDigits"
    }

    fun formatAttendance(attendanceUnits: Int, useBengaliDigits: Boolean = true): String {
        val value = attendanceUnits.toDouble() / ATTENDANCE_SCALE
        val rawStr = if (value % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", value)
        } else {
            String.format(Locale.US, "%.1f", value)
        }

        val digits = if (useBengaliDigits) convertToBengaliDigits(rawStr) else rawStr
        return "$digits হাজিরা"
    }

    fun takaToPoisha(takaStr: String): Long {
        val clean = takaStr.trim()
            .replace("৳", "")
            .replace(",", "")
            .let { s ->
                val sb = StringBuilder()
                for (ch in s) {
                    val idx = BENGALI_DIGITS.indexOf(ch)
                    if (idx != -1) {
                        sb.append(idx)
                    } else {
                        sb.append(ch)
                    }
                }
                sb.toString()
            }
        return try {
            val d = clean.toDouble()
            (d * POISHA_PER_TAKA).toLong()
        } catch (_: Exception) {
            0L
        }
    }

    fun poishaToTakaInput(poisha: Long): String {
        val taka = poisha / POISHA_PER_TAKA
        val rem = poisha % POISHA_PER_TAKA
        return if (rem > 0) "$taka.${String.format(Locale.US, "%02d", rem)}" else taka.toString()
    }

    fun calculateEarnedPoisha(attendanceUnits: Int, rateInPoisha: Long): Long {
        return (attendanceUnits.toLong() * rateInPoisha) / ATTENDANCE_SCALE
    }

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

    fun formatFullBengaliDate(isoDate: String, useBengaliDigits: Boolean = true): String {
        return try {
            val date = LocalDate.parse(isoDate)
            formatFullBengaliDate(date, useBengaliDigits)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatFullBengaliDate(date: LocalDate, useBengaliDigits: Boolean = true): String {
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

    fun formatYearMonthBengali(yearMonth: YearMonth, useBengaliDigits: Boolean = true): String {
        val monthNames = arrayOf(
            "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
            "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
        )
        val monthName = monthNames[yearMonth.monthValue - 1]
        val year = yearMonth.year.toString()
        val yearStr = if (useBengaliDigits) convertToBengaliDigits(year) else year
        return "$monthName $yearStr"
    }

    fun formatNumber(number: Long, useBengaliDigits: Boolean = true): String {
        val str = number.toString()
        return if (useBengaliDigits) convertToBengaliDigits(str) else str
    }
}
