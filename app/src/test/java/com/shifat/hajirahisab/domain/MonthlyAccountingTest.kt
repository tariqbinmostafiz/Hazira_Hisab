package com.shifat.hajirahisab.domain

import com.shifat.hajirahisab.data.local.entity.ExpenseEntity
import com.shifat.hajirahisab.data.local.entity.PaymentEntity
import com.shifat.hajirahisab.data.local.entity.WorkEntryEntity
import com.shifat.hajirahisab.domain.model.CurrencyFormatter
import com.shifat.hajirahisab.domain.model.MonthlyAccountingSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class MonthlyAccountingTest {

    @Test
    fun testCurrencyFormatterBengaliDigits() {
        val bengali = CurrencyFormatter.convertToBengaliDigits("123450")
        assertEquals("১২৩৪৫০", bengali)

        val takaFormatted = CurrencyFormatter.formatTaka(150000L, useBengaliDigits = true)
        assertEquals("৳১,৫০০", takaFormatted)

        val poisha = CurrencyFormatter.takaToPoisha("১,৫০০")
        assertEquals(150000L, poisha)
    }

    @Test
    fun testCurrentMonthDuesCalculationWithPreviousDuesAllocation() {
        val october = YearMonth.of(2026, 10)

        // September activity (Previous month):
        // Worked ৳10,000, received ৳4,000 -> Remaining dues from Sept = ৳6,000
        val septemberWorks = listOf(
            WorkEntryEntity(
                id = 1,
                date = "2026-09-10",
                attendanceUnits = 10000,
                rateInPoisha = 100000,
                earnedInPoisha = 1000000 // ৳10,000
            )
        )
        val septemberPayments = listOf(
            PaymentEntity(
                id = 1,
                date = "2026-09-15",
                amountInPoisha = 400000, // ৳4,000
                paymentTarget = PaymentEntity.TARGET_CURRENT_MONTH
            )
        )

        // October activity (Current month):
        // Worked ৳12,000
        // Payment 1: ৳6,000 explicitly for PREVIOUS_DUES (settling September)
        // Payment 2: ৳5,000 for CURRENT_MONTH
        // Expense: ৳1,000 deducted from dues
        val octoberWorks = listOf(
            WorkEntryEntity(
                id = 2,
                date = "2026-10-05",
                attendanceUnits = 12000,
                rateInPoisha = 100000,
                earnedInPoisha = 1200000 // ৳12,000
            )
        )
        val octoberPayments = listOf(
            PaymentEntity(
                id = 2,
                date = "2026-10-06",
                amountInPoisha = 600000, // ৳6,000
                paymentTarget = PaymentEntity.TARGET_PREVIOUS_DUES // Allocated to September!
            ),
            PaymentEntity(
                id = 3,
                date = "2026-10-12",
                amountInPoisha = 500000, // ৳5,000
                paymentTarget = PaymentEntity.TARGET_CURRENT_MONTH
            )
        )
        val octoberExpenses = listOf(
            ExpenseEntity(
                id = 1,
                date = "2026-10-08",
                amountInPoisha = 100000, // ৳1,000
                paymentSource = ExpenseEntity.SOURCE_DEDUCTED_FROM_DUES
            )
        )

        val previousEarned = septemberWorks.sumOf { it.earnedInPoisha } // 10,000
        val previousReceived = septemberPayments.sumOf { it.amountInPoisha } // 4,000

        val monthEarned = octoberWorks.sumOf { it.earnedInPoisha } // 12,000
        val monthReceived = octoberPayments.sumOf { it.amountInPoisha } // 11,000
        val monthPayCurrent = octoberPayments.filter { !it.isForPreviousDues }.sumOf { it.amountInPoisha } // 5,000
        val monthPayPrevious = octoberPayments.filter { it.isForPreviousDues }.sumOf { it.amountInPoisha } // 6,000
        val monthDeductedFromDues = octoberExpenses.filter { it.isDeductedFromDues }.sumOf { it.amountInPoisha } // 1,000

        val summary = MonthlyAccountingSummary(
            yearMonth = october,
            previousEarnedPoisha = previousEarned,
            previousReceivedPoisha = previousReceived,
            previousExpensePoisha = 0L,
            previousDeductedFromDuesPoisha = 0L,
            monthAttendanceUnits = 12000,
            monthEarnedPoisha = monthEarned,
            monthReceivedPoisha = monthReceived,
            monthPaymentsForCurrentMonthPoisha = monthPayCurrent,
            monthPaymentsForPreviousDuesPoisha = monthPayPrevious,
            monthExpensePoisha = 100000,
            monthDeductedFromDuesPoisha = monthDeductedFromDues
        )

        // 1. Opening balance: Previous dues = 10,000 - 4,000 = ৳6,000
        assertEquals(600000L, summary.previousDuesPoisha)

        // 2. Previous dues remaining after ৳6,000 payment = 0
        assertEquals(0L, summary.remainingPreviousDuesPoisha)

        // 3. Current month unpaid dues:
        // Formula: Current month earned (12,000) - current month payments (5,000) - dues deduction (1,000) = ৳6,000
        assertEquals(600000L, summary.monthUnpaidDuesPoisha)
        assertFalse(summary.isMonthAdvance)

        // 4. Total outstanding across all time:
        // Previous (6,000) + October earned (12,000) - Total received (11,000) - deduction (1,000) = ৳6,000
        assertEquals(600000L, summary.totalOutstandingPoisha)
        assertEquals(600000L, summary.totalUnpaidDuesPoisha)
    }

    @Test
    fun testAdvancePaymentCalculation() {
        val summary = MonthlyAccountingSummary(
            yearMonth = YearMonth.of(2026, 10),
            previousEarnedPoisha = 0L,
            previousReceivedPoisha = 0L,
            monthAttendanceUnits = 5000,
            monthEarnedPoisha = 500000L, // ৳5,000
            monthReceivedPoisha = 800000L, // ৳8,000 received
            monthPaymentsForCurrentMonthPoisha = 800000L,
            monthExpensePoisha = 0L,
            monthDeductedFromDuesPoisha = 0L
        )

        // Advance: ৳3,000 overpaid
        assertTrue(summary.isMonthAdvance)
        assertEquals(300000L, summary.monthAdvancePoisha)
        assertEquals(0L, summary.monthUnpaidDuesPoisha)
        assertEquals(300000L, summary.totalAdvancePoisha)
    }

    @Test
    fun testSection9TestA_NetIncome() {
        // Test A: October earned ৳8,000, food ৳900, other ৳300. Total expenses ৳1,200. Expected net income ৳6,800
        val earnedPoisha = 800000L // ৳8,000
        val foodExpense = 90000L   // ৳900
        val otherExpense = 30000L  // ৳300
        val totalExpense = foodExpense + otherExpense // ৳1,200

        val summary = MonthlyAccountingSummary(
            yearMonth = YearMonth.of(2026, 10),
            monthEarnedPoisha = earnedPoisha,
            monthReceivedPoisha = 200000L, // Payments received should NOT alter net income
            monthPaymentsForCurrentMonthPoisha = 200000L,
            monthExpensePoisha = totalExpense,
            monthFoodExpensePoisha = foodExpense,
            monthOtherExpensePoisha = otherExpense
        )

        assertEquals(120000L, summary.monthExpensePoisha)
        assertEquals(680000L, summary.monthNetIncomePoisha) // ৳6,800
    }

    @Test
    fun testSection9TestB_PaymentAgainstPreviousDebt() {
        // September: Earned ৳10,000, Payments allocated ৳6,000, Expenses deducted from dues ৳200
        // September outstanding = ৳3,800 (380,000 poisha)
        val septemberOutstanding = 1000000L - 600000L - 20000L
        assertEquals(380000L, septemberOutstanding)

        // October: Earned ৳5,000
        // Payment received against September debt: ৳2,000 (TARGET_PREVIOUS_DUES)
        // Expense deducted from October dues: ৳100
        // Normal expense: ৳500
        val octoberEarned = 500000L
        val octoberPayForSept = 200000L // PREVIOUS_DUES
        val octoberDeductedDues = 10000L
        val octoberNormalExpense = 50000L
        val octoberTotalExpense = octoberDeductedDues + octoberNormalExpense // ৳600

        val octoberSummary = MonthlyAccountingSummary(
            yearMonth = YearMonth.of(2026, 10),
            previousEarnedPoisha = 1000000L,
            previousReceivedPoisha = 600000L,
            previousDeductedFromDuesPoisha = 20000L,
            monthEarnedPoisha = octoberEarned,
            monthReceivedPoisha = octoberPayForSept, // Received in October
            monthPaymentsForCurrentMonthPoisha = 0L,  // 0 payments for October work
            monthPaymentsForPreviousDuesPoisha = octoberPayForSept, // ৳2,000 allocated to September
            monthExpensePoisha = octoberTotalExpense,
            monthDeductedFromDuesPoisha = octoberDeductedDues
        )

        // 1. September previous dues opening = ৳3,800
        assertEquals(380000L, octoberSummary.previousDuesPoisha)

        // 2. September remaining after ৳2,000 payment = ৳1,800
        assertEquals(180000L, octoberSummary.remainingPreviousDuesPoisha)

        // 3. Expected October outstanding: ৳5,000 - ৳100 = ৳4,900
        assertEquals(490000L, octoberSummary.monthUnpaidDuesPoisha)

        // 4. Combined remaining outstanding: ৳1,800 + ৳4,900 = ৳6,700
        assertEquals(670000L, octoberSummary.totalOutstandingPoisha)
        assertEquals(670000L, octoberSummary.totalUnpaidDuesPoisha)

        // 5. October net income: ৳5,000 - ৳100 - ৳500 = ৳4,400
        assertEquals(440000L, octoberSummary.monthNetIncomePoisha)
    }

    @Test
    fun testSection9TestC_NewMonthAutomaticSwitch() {
        val november = YearMonth.of(2026, 11)

        // When November begins with no records yet:
        val novemberSummary = MonthlyAccountingSummary(
            yearMonth = november,
            previousEarnedPoisha = 1500000L,
            previousReceivedPoisha = 800000L,
            previousDeductedFromDuesPoisha = 30000L,
            monthEarnedPoisha = 0L,
            monthReceivedPoisha = 0L,
            monthPaymentsForCurrentMonthPoisha = 0L,
            monthPaymentsForPreviousDuesPoisha = 0L,
            monthExpensePoisha = 0L,
            monthDeductedFromDuesPoisha = 0L
        )

        // November totals are zero
        assertEquals(0L, novemberSummary.monthEarnedPoisha)
        assertEquals(0L, novemberSummary.monthReceivedPoisha)
        assertEquals(0L, novemberSummary.monthExpensePoisha)
        assertEquals(0L, novemberSummary.monthNetIncomePoisha)
        assertEquals(0L, novemberSummary.monthUnpaidDuesPoisha)

        // Previous outstanding remains available
        val previousDues = 1500000L - 800000L - 30000L // 670,000 poisha
        assertEquals(previousDues, novemberSummary.previousDuesPoisha)
        assertEquals(previousDues, novemberSummary.totalOutstandingPoisha)
    }
}
