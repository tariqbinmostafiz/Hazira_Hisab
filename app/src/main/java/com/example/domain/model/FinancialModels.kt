package com.example.domain.model

data class FinancialSummary(
    val totalAttendanceUnits: Int = 0,
    val totalEarnedPoisha: Long = 0L,
    val totalReceivedPoisha: Long = 0L,
    val totalExpensePoisha: Long = 0L,
    val foodExpensePoisha: Long = 0L,
    val otherExpensePoisha: Long = 0L,
    val deductedFromDuesPoisha: Long = 0L
) {
    // Outstanding = Earned - Received - DeductedFromDues
    // If positive: unpaid dues from employer
    // If negative: advance / extra money received
    val outstandingPoisha: Long
        get() = totalEarnedPoisha - totalReceivedPoisha - deductedFromDuesPoisha

    val isAdvance: Boolean
        get() = (totalReceivedPoisha + deductedFromDuesPoisha) > totalEarnedPoisha

    val advancePoisha: Long
        get() = if (isAdvance) ((totalReceivedPoisha + deductedFromDuesPoisha) - totalEarnedPoisha) else 0L

    val unpaidDuesPoisha: Long
        get() = if (!isAdvance) (totalEarnedPoisha - totalReceivedPoisha - deductedFromDuesPoisha) else 0L

    // Cash position = Total Received - Cash Expenses (Total Expense - DeductedFromDues)
    // Critical: Special "deduct from dues" expense does NOT reduce cash in hand!
    val cashExpensesPoisha: Long
        get() = totalExpensePoisha - deductedFromDuesPoisha

    val cashInHandPoisha: Long
        get() = totalReceivedPoisha - cashExpensesPoisha

    val hasData: Boolean
        get() = totalAttendanceUnits > 0 || totalReceivedPoisha > 0L || totalExpensePoisha > 0L
}

data class DailySummary(
    val date: String,
    val attendanceUnits: Int = 0,
    val earnedPoisha: Long = 0L,
    val receivedPoisha: Long = 0L,
    val expensePoisha: Long = 0L,
    val deductedFromDuesPoisha: Long = 0L
) {
    val hasActivity: Boolean
        get() = attendanceUnits > 0 || receivedPoisha > 0L || expensePoisha > 0L
}

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
