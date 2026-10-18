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
}
