package com.shifat.hajirahisab.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shifat.hajirahisab.data.local.entity.ExpenseEntity
import com.shifat.hajirahisab.data.local.entity.PaymentEntity
import com.shifat.hajirahisab.data.local.entity.WorkEntryEntity
import com.shifat.hajirahisab.data.repository.LedgerRepository
import com.shifat.hajirahisab.domain.model.CurrencyFormatter
import com.shifat.hajirahisab.domain.model.DailySummary
import com.shifat.hajirahisab.domain.model.FinancialSummary
import com.shifat.hajirahisab.domain.model.MonthlyAccountingSummary
import com.shifat.hajirahisab.domain.model.RecentActivityItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

enum class ReportPeriod(val title: String) {
    THIS_MONTH("এই মাস"),
    LAST_MONTH("গত মাস"),
    THIS_YEAR("এই বছর"),
    ALL_TIME("সর্বমোট")
}

sealed class DeletedItem {
    data class Work(val entry: WorkEntryEntity) : DeletedItem()
    data class Payment(val payment: PaymentEntity) : DeletedItem()
    data class Expense(val expense: ExpenseEntity) : DeletedItem()
}

class LedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LedgerRepository(application)

    // User preferences state
    private val _themeMode = MutableStateFlow(repository.getThemeMode())
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _useBengaliDigits = MutableStateFlow(repository.getUseBengaliDigits())
    val useBengaliDigits: StateFlow<Boolean> = _useBengaliDigits.asStateFlow()

    private val _defaultRatePoisha = MutableStateFlow(repository.getDefaultRatePoisha())
    val defaultRatePoisha: StateFlow<Long> = _defaultRatePoisha.asStateFlow()

    // Snackbar notifications
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // Undo last delete
    private var lastDeletedItem: DeletedItem? = null

    // Raw entity flows from database
    val workEntries: StateFlow<List<WorkEntryEntity>> = repository.getWorkEntriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> = repository.getPaymentsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.getExpensesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unified activities list
    val recentActivities: StateFlow<List<RecentActivityItem>> = repository.getRecentActivitiesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's summary (Home card)
    val todaySummary: StateFlow<DailySummary> = repository.getTodaySummaryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailySummary(CurrencyFormatter.todayIso()))

    // Overall summary across all time
    val overallSummary: StateFlow<FinancialSummary> = repository.getOverallSummaryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    // Selected month for Reports screen
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    // Monthly Accounting Summary for Reports (Selected Month)
    val monthlySummary: StateFlow<MonthlyAccountingSummary> = combine(
        _selectedMonth,
        workEntries,
        payments,
        expenses
    ) { month, works, pays, exps ->
        calculateMonthlySummary(month, works, pays, exps)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlyAccountingSummary(YearMonth.now())
    )

    // Current Month Summary strictly for Home Screen (Auto-updating based on calendar)
    val currentMonthSummary: StateFlow<MonthlyAccountingSummary> = combine(
        workEntries,
        payments,
        expenses
    ) { works, pays, exps ->
        calculateMonthlySummary(YearMonth.now(), works, pays, exps)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlyAccountingSummary(YearMonth.now())
    )

    // Current month recent activities for Home Screen
    val currentMonthActivities: StateFlow<List<RecentActivityItem>> = recentActivities
        .map { activities ->
            val currentYearMonthStr = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            activities.filter { it.date.startsWith(currentYearMonthStr) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Activities strictly for selected month (Reports Screen)
    val monthActivities: StateFlow<List<RecentActivityItem>> = combine(
        _selectedMonth,
        recentActivities
    ) { month, activities ->
        val prefix = month.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        activities.filter { it.date.startsWith(prefix) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Available months in database (sorted newest first)
    val availableMonths: StateFlow<List<YearMonth>> = combine(
        workEntries,
        payments,
        expenses
    ) { works, pays, exps ->
        val set = mutableSetOf<YearMonth>()
        set.add(YearMonth.now())

        for (w in works) {
            try { set.add(YearMonth.parse(w.date.substring(0, 7))) } catch (_: Exception) {}
        }
        for (p in pays) {
            try { set.add(YearMonth.parse(p.date.substring(0, 7))) } catch (_: Exception) {}
        }
        for (e in exps) {
            try { set.add(YearMonth.parse(e.date.substring(0, 7))) } catch (_: Exception) {}
        }

        set.sortedDescending()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(YearMonth.now()))

    // Report Period Selection
    private val _selectedPeriod = MutableStateFlow(ReportPeriod.THIS_MONTH)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    // Period Summary Flow
    val periodSummary: StateFlow<FinancialSummary> = combine(
        _selectedPeriod,
        workEntries,
        payments,
        expenses
    ) { period, works, pays, exps ->
        val today = LocalDate.now()
        val (start, end) = when (period) {
            ReportPeriod.THIS_MONTH -> {
                val startOfMonth = today.withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                val endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).format(DateTimeFormatter.ISO_LOCAL_DATE)
                startOfMonth to endOfMonth
            }
            ReportPeriod.LAST_MONTH -> {
                val lastMonth = today.minusMonths(1)
                val startOfLast = lastMonth.withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                val endOfLast = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()).format(DateTimeFormatter.ISO_LOCAL_DATE)
                startOfLast to endOfLast
            }
            ReportPeriod.THIS_YEAR -> {
                val startOfYear = today.withDayOfYear(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                val endOfYear = today.withDayOfYear(today.lengthOfYear()).format(DateTimeFormatter.ISO_LOCAL_DATE)
                startOfYear to endOfYear
            }
            ReportPeriod.ALL_TIME -> "1970-01-01" to "2099-12-31"
        }

        var attUnits = 0
        var earned = 0L
        var received = 0L
        var expense = 0L
        var food = 0L
        var other = 0L
        var duesDeduction = 0L

        var wCount = 0
        var pCount = 0
        var eCount = 0

        for (w in works) {
            if (w.date in start..end) {
                attUnits += w.attendanceUnits
                earned += w.earnedInPoisha
                wCount++
            }
        }
        for (p in pays) {
            if (p.date in start..end) {
                received += p.amountInPoisha
                pCount++
            }
        }
        for (e in exps) {
            if (e.date in start..end) {
                expense += e.amountInPoisha
                if (e.category == "খাবার") {
                    food += e.amountInPoisha
                } else {
                    other += e.amountInPoisha
                }
                if (e.isDeductedFromDues) {
                    duesDeduction += e.amountInPoisha
                }
                eCount++
            }
        }

        FinancialSummary(
            totalAttendanceUnits = attUnits,
            totalEarnedPoisha = earned,
            totalReceivedPoisha = received,
            totalExpensePoisha = expense,
            foodExpensePoisha = food,
            otherExpensePoisha = other,
            deductedFromDuesPoisha = duesDeduction,
            workCount = wCount,
            paymentCount = pCount,
            expenseCount = eCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    // Month Navigation in Reports
    fun selectMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    fun goToPreviousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun goToNextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun resetToCurrentMonth() {
        _selectedMonth.value = YearMonth.now()
    }

    fun selectPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
    }

    // Work Operations
    fun saveWorkEntry(
        id: Long = 0L,
        date: String,
        attendanceUnits: Int,
        rateInPoisha: Long,
        workType: String,
        note: String
    ) {
        viewModelScope.launch {
            val earned = CurrencyFormatter.calculateEarnedPoisha(attendanceUnits, rateInPoisha)
            val entry = WorkEntryEntity(
                id = id,
                date = date,
                attendanceUnits = attendanceUnits,
                rateInPoisha = rateInPoisha,
                earnedInPoisha = earned,
                workType = workType,
                note = note
            )
            repository.saveWorkEntry(entry)
            _snackbarMessage.emit("কাজের হাজিরা সংরক্ষণ করা হয়েছে")
        }
    }

    fun deleteWorkEntry(entry: WorkEntryEntity) {
        viewModelScope.launch {
            lastDeletedItem = DeletedItem.Work(entry)
            repository.deleteWorkEntry(entry)
            _snackbarMessage.emit("হাজিরা মুছে ফেলা হয়েছে")
        }
    }

    // Payment Operations
    fun savePayment(
        id: Long = 0L,
        date: String,
        amountInPoisha: Long,
        payer: String,
        note: String,
        paymentTarget: String = PaymentEntity.TARGET_CURRENT_MONTH
    ) {
        viewModelScope.launch {
            val payment = PaymentEntity(
                id = id,
                date = date,
                amountInPoisha = amountInPoisha,
                payer = payer,
                note = note,
                paymentTarget = paymentTarget
            )
            repository.savePayment(payment)
            _snackbarMessage.emit("টাকা পাওয়ার হিসাব সংরক্ষণ করা হয়েছে")
        }
    }

    fun deletePayment(payment: PaymentEntity) {
        viewModelScope.launch {
            lastDeletedItem = DeletedItem.Payment(payment)
            repository.deletePayment(payment)
            _snackbarMessage.emit("টাকা পাওয়ার হিসাব মুছে ফেলা হয়েছে")
        }
    }

    // Expense Operations
    fun saveExpense(
        id: Long = 0L,
        date: String,
        amountInPoisha: Long,
        category: String,
        note: String,
        paymentSource: String = ExpenseEntity.SOURCE_CASH
    ) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                id = id,
                date = date,
                amountInPoisha = amountInPoisha,
                category = category,
                note = note,
                paymentSource = paymentSource
            )
            repository.saveExpense(expense)
            _snackbarMessage.emit("খরচের হিসাব সংরক্ষণ করা হয়েছে")
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            lastDeletedItem = DeletedItem.Expense(expense)
            repository.deleteExpense(expense)
            _snackbarMessage.emit("খরচের হিসাব মুছে ফেলা হয়েছে")
        }
    }

    // Undo delete
    fun undoLastDelete() {
        val item = lastDeletedItem ?: return
        viewModelScope.launch {
            when (item) {
                is DeletedItem.Work -> repository.saveWorkEntry(item.entry.copy(id = 0L))
                is DeletedItem.Payment -> repository.savePayment(item.payment.copy(id = 0L))
                is DeletedItem.Expense -> repository.saveExpense(item.expense.copy(id = 0L))
            }
            lastDeletedItem = null
            _snackbarMessage.emit("মুছে ফেলা হিসাবটি পুনরুদ্ধার করা হয়েছে")
        }
    }

    // Preferences Operations
    fun updateDefaultRate(rateInPoisha: Long) {
        repository.setDefaultRatePoisha(rateInPoisha)
        _defaultRatePoisha.value = rateInPoisha
        viewModelScope.launch {
            _snackbarMessage.emit("ডিফল্ট রেট আপডেট করা হয়েছে")
        }
    }

    fun updateThemeMode(mode: String) {
        repository.setThemeMode(mode)
        _themeMode.value = mode
    }

    fun updateBengaliDigits(use: Boolean) {
        repository.setUseBengaliDigits(use)
        _useBengaliDigits.value = use
    }

    fun loadDemoData() {
        viewModelScope.launch {
            repository.loadDemoData()
            _snackbarMessage.emit("ডেমো তথ্য সফলভাবে লোড করা হয়েছে")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _snackbarMessage.emit("সকল তথ্য মুছে ফেলা হয়েছে")
        }
    }

    // Backup & Restore
    suspend fun getBackupJson(): String {
        return repository.exportBackupJson()
    }

    suspend fun restoreBackup(jsonString: String) {
        val result = repository.restoreBackupJson(jsonString, replaceExisting = true)
        result.onSuccess { count ->
            _snackbarMessage.emit("সফলভাবে $count টি হিসাব পুনরুদ্ধার করা হয়েছে")
        }.onFailure { error ->
            _snackbarMessage.emit("রিস্টোর ব্যর্থ: ${error.localizedMessage}")
        }
    }

    /**
     * Complete Deterministic Monthly Accounting Calculation
     *
     * Correctly handles:
     * 1. Opening balance of previous dues from all earlier months
     * 2. Allocation of payments to previous dues vs current month
     * 3. Current month's dues (earned - payments for current - deductions from dues)
     * 4. Monthly Net Income (earned - total recorded expenses)
     */
    fun calculateMonthlySummary(
        month: YearMonth,
        works: List<WorkEntryEntity>,
        pays: List<PaymentEntity>,
        exps: List<ExpenseEntity>
    ): MonthlyAccountingSummary {
        val startOfMonth = month.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val endOfMonth = month.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE)

        var prevEarned = 0L
        var prevReceived = 0L
        var prevExpense = 0L
        var prevDuesDeduction = 0L

        var monthAtt = 0
        var monthEarned = 0L
        var monthReceived = 0L
        var monthPaymentsForCurrent = 0L
        var monthPaymentsForPrevious = 0L
        var monthExpense = 0L
        var monthFood = 0L
        var monthOther = 0L
        var monthDuesDeduction = 0L

        for (w in works) {
            if (w.date < startOfMonth) {
                prevEarned += w.earnedInPoisha
            } else if (w.date <= endOfMonth) {
                monthAtt += w.attendanceUnits
                monthEarned += w.earnedInPoisha
            }
        }

        for (p in pays) {
            if (p.date < startOfMonth) {
                prevReceived += p.amountInPoisha
            } else if (p.date <= endOfMonth) {
                monthReceived += p.amountInPoisha
                if (p.paymentTarget == PaymentEntity.TARGET_PREVIOUS_DUES) {
                    monthPaymentsForPrevious += p.amountInPoisha
                } else {
                    monthPaymentsForCurrent += p.amountInPoisha
                }
            }
        }

        for (e in exps) {
            if (e.date < startOfMonth) {
                prevExpense += e.amountInPoisha
                if (e.isDeductedFromDues) {
                    prevDuesDeduction += e.amountInPoisha
                }
            } else if (e.date <= endOfMonth) {
                monthExpense += e.amountInPoisha
                if (e.category == "খাবার") {
                    monthFood += e.amountInPoisha
                } else {
                    monthOther += e.amountInPoisha
                }
                if (e.isDeductedFromDues) {
                    monthDuesDeduction += e.amountInPoisha
                }
            }
        }

        return MonthlyAccountingSummary(
            yearMonth = month,
            previousEarnedPoisha = prevEarned,
            previousReceivedPoisha = prevReceived,
            previousExpensePoisha = prevExpense,
            previousDeductedFromDuesPoisha = prevDuesDeduction,
            monthAttendanceUnits = monthAtt,
            monthEarnedPoisha = monthEarned,
            monthReceivedPoisha = monthReceived,
            monthPaymentsForCurrentMonthPoisha = monthPaymentsForCurrent,
            monthPaymentsForPreviousDuesPoisha = monthPaymentsForPrevious,
            monthExpensePoisha = monthExpense,
            monthFoodExpensePoisha = monthFood,
            monthOtherExpensePoisha = monthOther,
            monthDeductedFromDuesPoisha = monthDuesDeduction
        )
    }
}
