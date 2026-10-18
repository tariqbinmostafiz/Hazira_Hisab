package com.shifat.hajirahisab.domain.model

import java.time.YearMonth

enum class TransactionType {
    WORK,
    PAYMENT,
    EXPENSE
}

data class RecentActivityItem(
    val id: Long,
    val type: TransactionType,
    val date: String,
    val title: String,
    val subtitle: String,
    val amountPoisha: Long,
    val timestamp: Long
)

data class DailySummary(
    val date: String,
    val attendanceUnits: Int = 0,
    val earnedPoisha: Long = 0L,
    val receivedPoisha: Long = 0L,
    val expensePoisha: Long = 0L
)

data class FinancialSummary(
    val totalAttendanceUnits: Int = 0,
    val totalEarnedPoisha: Long = 0L,
    val totalReceivedPoisha: Long = 0L,
    val totalExpensePoisha: Long = 0L,
    val foodExpensePoisha: Long = 0L,
    val otherExpensePoisha: Long = 0L,
    val deductedFromDuesPoisha: Long = 0L,
    val workCount: Int = 0,
    val paymentCount: Int = 0,
    val expenseCount: Int = 0
) {
    val outstandingPoisha: Long
        get() = totalEarnedPoisha - totalReceivedPoisha - deductedFromDuesPoisha

    val isAdvance: Boolean
        get() = outstandingPoisha < 0L

    val advancePoisha: Long
        get() = if (isAdvance) -outstandingPoisha else 0L

    val unpaidDuesPoisha: Long
        get() = if (!isAdvance) outstandingPoisha else 0L

    // Net Income = Total Earned - Total Expenses
    val netIncomePoisha: Long
        get() = totalEarnedPoisha - totalExpensePoisha
}

data class MonthlyAccountingSummary(
    val yearMonth: YearMonth,
    // Opening balances (all records prior to this month)
    val previousEarnedPoisha: Long = 0L,
    val previousReceivedPoisha: Long = 0L,
    val previousExpensePoisha: Long = 0L,
    val previousDeductedFromDuesPoisha: Long = 0L,

    // Activity in this month
    val monthAttendanceUnits: Int = 0,
    val monthEarnedPoisha: Long = 0L,
    val monthReceivedPoisha: Long = 0L,
    val monthPaymentsForCurrentMonthPoisha: Long = monthReceivedPoisha,
    val monthPaymentsForPreviousDuesPoisha: Long = 0L,
    val monthExpensePoisha: Long = 0L,
    val monthFoodExpensePoisha: Long = 0L,
    val monthOtherExpensePoisha: Long = 0L,
    val monthDeductedFromDuesPoisha: Long = 0L
) {
    // 1. Opening Dues Balance (পূর্বের বকেয়া)
    val previousDuesPoisha: Long
        get() = previousEarnedPoisha - previousReceivedPoisha - previousDeductedFromDuesPoisha

    val isPreviousAdvance: Boolean
        get() = previousDuesPoisha < 0L

    val previousAdvancePoisha: Long
        get() = if (isPreviousAdvance) -previousDuesPoisha else 0L

    val previousUnpaidDuesPoisha: Long
        get() = if (!isPreviousAdvance) previousDuesPoisha else 0L

    // Remaining previous dues after payments explicitly allocated to previous dues
    val remainingPreviousDuesPoisha: Long
        get() = (previousDuesPoisha - monthPaymentsForPreviousDuesPoisha).coerceAtLeast(0L)

    // 2. Current Month's Dues Calculation (চলতি মাসের নিজস্ব বাকি পাওনা)
    // Formula: current-month earned income - payments allocated to current-month earnings - expenses explicitly deducted from current-month dues
    val monthNetDueChangePoisha: Long
        get() = monthEarnedPoisha - monthPaymentsForCurrentMonthPoisha - monthDeductedFromDuesPoisha

    val isMonthAdvance: Boolean
        get() = monthNetDueChangePoisha < 0L

    val monthAdvancePoisha: Long
        get() = if (isMonthAdvance) -monthNetDueChangePoisha else 0L

    val monthUnpaidDuesPoisha: Long
        get() = if (!isMonthAdvance) monthNetDueChangePoisha else 0L

    // 3. Total Outstanding / Closing Due Across All Time (সর্বমোট বকেয়া পাওনা)
    val totalOutstandingPoisha: Long
        get() = previousDuesPoisha + monthEarnedPoisha - monthReceivedPoisha - monthDeductedFromDuesPoisha

    val isTotalAdvance: Boolean
        get() = totalOutstandingPoisha < 0L

    val totalAdvancePoisha: Long
        get() = if (isTotalAdvance) -totalOutstandingPoisha else 0L

    val totalUnpaidDuesPoisha: Long
        get() = if (!isTotalAdvance) totalOutstandingPoisha else 0L

    // 4. Monthly Net Income: Earned Income - Total Recorded Expenses
    // (All expenses including dues-deducted count toward expenses and net income)
    val monthNetIncomePoisha: Long
        get() = monthEarnedPoisha - monthExpensePoisha

    val hasMonthActivity: Boolean
        get() = monthAttendanceUnits > 0 || monthReceivedPoisha > 0L || monthExpensePoisha > 0L

    val hasPreviousData: Boolean
        get() = previousEarnedPoisha > 0L || previousReceivedPoisha > 0L || previousExpensePoisha > 0L
}
