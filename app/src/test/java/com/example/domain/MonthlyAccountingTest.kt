package com.example.domain

import com.example.domain.model.CurrencyFormatter
import com.example.domain.model.MonthlyAccountingSummary
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class MonthlyAccountingTest {

    @Test
    fun testMonthlyAccountingDuesCarryForward() {
        // User example from requirements:
        // September:
        // Earned = 10,000 (1000000 poisha)
        // Received = 6,000 (600000 poisha)
        // Outstanding = 4,000 (400000 poisha)
        //
        // October:
        // Earned = 5,000 (500000 poisha)
        // Received = 2,000 (200000 poisha)
        // Deducted from dues = 500 (50000 poisha)
        //
        // Current total outstanding: 4,000 + 5,000 - 2,000 - 500 = 6,500 (650000 poisha)

        val summary = MonthlyAccountingSummary(
            yearMonth = YearMonth.of(2026, 10),
            previousEarnedPoisha = 10_000_00L,
            previousReceivedPoisha = 6_000_00L,
            previousExpensePoisha = 1_000_00L,
            previousDeductedFromDuesPoisha = 0L,
            monthAttendanceUnits = 8000,
            monthEarnedPoisha = 5_000_00L,
            monthReceivedPoisha = 2_000_00L,
            monthExpensePoisha = 800_00L,
            monthFoodExpensePoisha = 480_00L,
            monthOtherExpensePoisha = 320_00L,
            monthDeductedFromDuesPoisha = 500_00L
        )

        // Previous dues
        assertEquals(4_000_00L, summary.previousDuesPoisha)
        assertEquals(false, summary.isPreviousAdvance)

        // Month net due change
        assertEquals(2_500_00L, summary.monthNetDueChangePoisha)

        // Total closing outstanding
        assertEquals(6_500_00L, summary.totalOutstandingPoisha)
        assertEquals(6_500_00L, summary.totalUnpaidDuesPoisha)
        assertEquals(false, summary.isTotalAdvance)
    }

    @Test
    fun testMonthlyAccountingCashCarryForward() {
        // September:
        // Received = 6,000 (600000 poisha)
        // Cash Expense = 1,000 (100000 poisha)
        // Previous cash in hand = 5,000 (500000 poisha)
        //
        // October:
        // Received = 2,000 (200000 poisha)
        // Total Expense = 800 (500 deducted from dues, 300 cash)
        // Deducted from dues = 500
        // Month cash expenses = 300 (30000 poisha)
        // Total cash in hand = 5,000 + 2,000 - 300 = 6,700 (670000 poisha)

        val summary = MonthlyAccountingSummary(
            yearMonth = YearMonth.of(2026, 10),
            previousEarnedPoisha = 10_000_00L,
            previousReceivedPoisha = 6_000_00L,
            previousExpensePoisha = 1_000_00L,
            previousDeductedFromDuesPoisha = 0L,
            monthAttendanceUnits = 8000,
            monthEarnedPoisha = 5_000_00L,
            monthReceivedPoisha = 2_000_00L,
            monthExpensePoisha = 800_00L,
            monthFoodExpensePoisha = 500_00L,
            monthOtherExpensePoisha = 300_00L,
            monthDeductedFromDuesPoisha = 500_00L
        )

        assertEquals(5_000_00L, summary.previousCashInHandPoisha)
        assertEquals(300_00L, summary.monthCashExpensesPoisha)
        assertEquals(1_700_00L, summary.monthNetCashChangePoisha)
        assertEquals(6_700_00L, summary.totalCashInHandPoisha)
    }

    @Test
    fun testBengaliMonthFormatting() {
        val oct2026 = CurrencyFormatter.formatYearMonthBengali(YearMonth.of(2026, 10), useBengaliDigits = true)
        assertEquals("অক্টোবর ২০২৬", oct2026)

        val sep2026 = CurrencyFormatter.formatYearMonthBengali(YearMonth.of(2026, 9), useBengaliDigits = true)
        assertEquals("সেপ্টেম্বর ২০২৬", sep2026)
    }

    @Test
    fun testUserSection21Scenario() {
        val octSummary = MonthlyAccountingSummary(
            yearMonth = YearMonth.of(2026, 10),
            previousEarnedPoisha = 10_000_00L,
            previousReceivedPoisha = 6_000_00L,
            previousExpensePoisha = 1_200_00L,
            previousDeductedFromDuesPoisha = 200_00L,
            monthAttendanceUnits = 5000,
            monthEarnedPoisha = 5_000_00L,
            monthReceivedPoisha = 2_000_00L,
            monthExpensePoisha = 600_00L,
            monthFoodExpensePoisha = 350_00L,
            monthOtherExpensePoisha = 250_00L,
            monthDeductedFromDuesPoisha = 100_00L
        )

        // HOME numbers
        assertEquals(5000, octSummary.monthAttendanceUnits)
        assertEquals(5_000_00L, octSummary.monthEarnedPoisha)
        assertEquals(2_000_00L, octSummary.monthReceivedPoisha)
        assertEquals(600_00L, octSummary.monthExpensePoisha)
        // Home card "মোট বাকি পাওনা" (current month only): 5000 - 2000 - 100 = 2900 Tk
        assertEquals(2_900_00L, octSummary.monthUnpaidDuesPoisha)

        // REPORTS numbers
        assertEquals(3_800_00L, octSummary.previousDuesPoisha)
        assertEquals(5_000_00L, octSummary.previousCashInHandPoisha)
        assertEquals(500_00L, octSummary.monthCashExpensesPoisha)
        assertEquals(6_700_00L, octSummary.totalUnpaidDuesPoisha)
        assertEquals(6_500_00L, octSummary.totalCashInHandPoisha)
    }

    @Test
    fun testPaymentAllocationToPreviousDuesVsCurrentMonth() {
        // September:
        // Earned = 10,000 Tk, Received = 6,000 Tk -> Previous dues = 4,000 Tk
        //
        // October:
        // Earned = 5,000 Tk
        // Total received = 4,000 Tk, where:
        //   - 3,000 Tk allocated to PREVIOUS DUES
        //   - 1,000 Tk allocated to CURRENT MONTH
        // Deducted from dues = 500 Tk
        //
        // Home Screen "মোট বাকি পাওনা" (October only):
        // October Earned (5,000) - October Received for October (1,000) - Deductions (500) = 3,500 Tk
        //
        // Reports Screen:
        // Previous dues = 4,000 Tk
        // Previous dues paid this month = 3,000 Tk
        // Remaining previous dues = 1,000 Tk
        // Current month dues = 3,500 Tk
        // Total closing dues = 1,000 + 3,500 = 4,500 Tk

        val octSummary = MonthlyAccountingSummary(
            yearMonth = YearMonth.of(2026, 10),
            previousEarnedPoisha = 10_000_00L,
            previousReceivedPoisha = 6_000_00L,
            previousExpensePoisha = 1_000_00L,
            previousDeductedFromDuesPoisha = 0L,
            monthAttendanceUnits = 8000,
            monthEarnedPoisha = 5_000_00L,
            monthReceivedPoisha = 4_000_00L,
            monthPaymentsForCurrentMonthPoisha = 1_000_00L,
            monthPaymentsForPreviousDuesPoisha = 3_000_00L,
            monthExpensePoisha = 800_00L,
            monthFoodExpensePoisha = 500_00L,
            monthOtherExpensePoisha = 300_00L,
            monthDeductedFromDuesPoisha = 500_00L
        )

        // Home screen values (Current month only)
        assertEquals(5_000_00L, octSummary.monthEarnedPoisha)
        assertEquals(4_000_00L, octSummary.monthReceivedPoisha)
        assertEquals(3_500_00L, octSummary.monthUnpaidDuesPoisha)
        assertEquals(false, octSummary.isMonthAdvance)

        // Reports screen values
        assertEquals(4_000_00L, octSummary.previousDuesPoisha)
        assertEquals(3_000_00L, octSummary.monthPaymentsForPreviousDuesPoisha)
        assertEquals(1_000_00L, octSummary.remainingPreviousDuesPoisha)
        assertEquals(4_500_00L, octSummary.totalUnpaidDuesPoisha)
    }
}
