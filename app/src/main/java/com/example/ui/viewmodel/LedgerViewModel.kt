package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DataStoreManager
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.WorkEntryEntity
import com.example.data.repository.LedgerRepository
import com.example.domain.ai.GeminiFinancialAdvisor
import com.example.domain.model.CurrencyFormatter
import com.example.domain.model.DailySummary
import com.example.domain.model.FinancialSummary
import com.example.domain.model.MonthlyAccountingSummary
import com.example.domain.model.RecentActivityItem
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

enum class ReportPeriod(val label: String) {
    TODAY("আজ"),
    THIS_WEEK("এই সপ্তাহ"),
    THIS_MONTH("এই মাস"),
    LAST_MONTH("গত মাস"),
    ALL_TIME("সব সময়")
}

sealed class DeletedItem {
    data class Work(val entry: WorkEntryEntity) : DeletedItem()
    data class Payment(val payment: PaymentEntity) : DeletedItem()
    data class Expense(val expense: ExpenseEntity) : DeletedItem()
}

class LedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dataStore = DataStoreManager(application)
    val repository = LedgerRepository(db, dataStore)
    private val aiAdvisor = GeminiFinancialAdvisor()

    // Overall & Today's Summaries
    val overallSummary: StateFlow<FinancialSummary> = repository.getOverallSummaryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    val todaySummary: StateFlow<DailySummary> = repository.getTodaySummaryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailySummary(CurrencyFormatter.todayIso()))

    // Lists
    val workEntries: StateFlow<List<WorkEntryEntity>> = repository.getAllWorkEntriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> = repository.getAllPaymentsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.getAllExpensesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentActivities: StateFlow<List<RecentActivityItem>> = repository.getRecentActivitiesFlow(15)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Preferences
    val defaultRatePoisha: StateFlow<Long> = dataStore.defaultRatePoisha
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DataStoreManager.DEFAULT_RATE_POISHA)

    val themeMode: StateFlow<String> = dataStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val useBengaliDigits: StateFlow<Boolean> = dataStore.useBengaliDigits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Monthly Accounting Period State (defaults automatically to current month)
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    // Available months list extracted dynamically from records + current month (sorted descending)
    val availableMonths: StateFlow<List<YearMonth>> = combine(
        workEntries,
        payments,
        expenses,
        _selectedMonth
    ) { works, pays, exps, selected ->
        val set = mutableSetOf<YearMonth>()
        set.add(YearMonth.now())
        set.add(selected)
        works.forEach { w -> CurrencyFormatter.parseYearMonth(w.date)?.let { set.add(it) } }
        pays.forEach { p -> CurrencyFormatter.parseYearMonth(p.date)?.let { set.add(it) } }
        exps.forEach { e -> CurrencyFormatter.parseYearMonth(e.date)?.let { set.add(it) } }
        set.sortedDescending()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(YearMonth.now()))

    // Monthly Accounting Summary for Reports: carries forward opening balances and calculates selected month's financials
    val monthlySummary: StateFlow<MonthlyAccountingSummary> = combine(
        _selectedMonth,
        workEntries,
        payments,
        expenses
    ) { month, works, pays, exps ->
        calculateMonthlySummary(month, works, pays, exps)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlyAccountingSummary(YearMonth.now()))

    // Current Month Summary for HOME screen: ALWAYS calculates for YearMonth.now()
    val currentMonthSummary: StateFlow<MonthlyAccountingSummary> = combine(
        workEntries,
        payments,
        expenses
    ) { works, pays, exps ->
        calculateMonthlySummary(YearMonth.now(), works, pays, exps)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlyAccountingSummary(YearMonth.now()))

    // Recent activities filtered for the selected month in Reports
    val monthActivities: StateFlow<List<RecentActivityItem>> = combine(
        _selectedMonth,
        recentActivities
    ) { month, activities ->
        val prefix = month.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val monthFiltered = activities.filter { it.date.startsWith(prefix) }
        monthFiltered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recent activities filtered for the current month on HOME screen
    val currentMonthActivities: StateFlow<List<RecentActivityItem>> = recentActivities
        .map { activities ->
            val prefix = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            activities.filter { it.date.startsWith(prefix) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
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

    // Report Period
    private val _selectedPeriod = MutableStateFlow(ReportPeriod.THIS_MONTH)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    // Period Summary calculation
    val periodSummary: StateFlow<FinancialSummary> = combine(
        _selectedPeriod,
        workEntries,
        payments,
        expenses
    ) { period, works, pays, exps ->
        calculatePeriodSummary(period, works, pays, exps)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    // UI Feedback Event
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // Undo Buffer
    private var lastDeletedItem: DeletedItem? = null

    // AI Advisor State
    private val _aiAnalysis = MutableStateFlow<String?>(null)
    val aiAnalysis: StateFlow<String?> = _aiAnalysis.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun selectPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
    }

    // Work Entry Operations
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
            _snackbarMessage.emit("হাজিরার হিসাব সংরক্ষণ করা হয়েছে")
        }
    }

    fun deleteWorkEntry(entry: WorkEntryEntity) {
        viewModelScope.launch {
            lastDeletedItem = DeletedItem.Work(entry)
            repository.deleteWorkEntry(entry)
            _snackbarMessage.emit("কাজের হিসাব মুছে ফেলা হয়েছে")
        }
    }

    // Payment Operations
    fun savePayment(
        id: Long = 0L,
        date: String,
        amountInPoisha: Long,
        payer: String,
        note: String
    ) {
        viewModelScope.launch {
            val payment = PaymentEntity(
                id = id,
                date = date,
                amountInPoisha = amountInPoisha,
                payer = payer,
                note = note
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

    // Undo Last Delete
    fun undoLastDelete() {
        viewModelScope.launch {
            val item = lastDeletedItem ?: return@launch
            when (item) {
                is DeletedItem.Work -> repository.saveWorkEntry(item.entry.copy(id = 0L))
                is DeletedItem.Payment -> repository.savePayment(item.payment.copy(id = 0L))
                is DeletedItem.Expense -> repository.saveExpense(item.expense.copy(id = 0L))
            }
            lastDeletedItem = null
            _snackbarMessage.emit("হিসাব পুনরুদ্ধার করা হয়েছে")
        }
    }

    // Settings Updates
    fun updateDefaultRate(ratePoisha: Long) {
        viewModelScope.launch {
            dataStore.setDefaultRatePoisha(ratePoisha)
            _snackbarMessage.emit("১ হাজিরার নতুন রেট নির্ধারিত হয়েছে")
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            dataStore.setThemeMode(mode)
        }
    }

    fun updateBengaliDigits(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setUseBengaliDigits(enabled)
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            repository.loadDemoData()
            _snackbarMessage.emit("নমুনা ডেমো ডাটা সফলভাবে যোগ হয়েছে")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _snackbarMessage.emit("সমস্ত ডাটা সম্পূর্ণ মুছে ফেলা হয়েছে")
        }
    }

    fun runAiAnalysis() {
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val summary = periodSummary.value
                val analysis = aiAdvisor.analyzeLedger(summary)
                _aiAnalysis.value = analysis
            } catch (_: Exception) {
                _aiAnalysis.value = "বিশ্লেষণ করতে সমস্যা হয়েছে। দয়া করে আবার চেষ্টা করুন।"
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    suspend fun getBackupJson(): String {
        return repository.exportBackupJson()
    }

    suspend fun restoreBackup(json: String): Result<Int> {
        val res = repository.restoreBackupJson(json, replaceExisting = true)
        if (res.isSuccess) {
            _snackbarMessage.emit("ব্যাকআপ সফলভাবে পুনরুদ্ধার করা হয়েছে (${res.getOrNull()} টি এন্ট্রি)")
        }
        return res
    }

    private fun calculatePeriodSummary(
        period: ReportPeriod,
        works: List<WorkEntryEntity>,
        pays: List<PaymentEntity>,
        exps: List<ExpenseEntity>
    ): FinancialSummary {
        val today = LocalDate.now()
        val (startDate, endDate) = when (period) {
            ReportPeriod.TODAY -> Pair(today, today)
            ReportPeriod.THIS_WEEK -> Pair(today.minusDays(today.dayOfWeek.value.toLong() - 1), today)
            ReportPeriod.THIS_MONTH -> Pair(today.withDayOfMonth(1), today)
            ReportPeriod.LAST_MONTH -> {
                val lastMonth = today.minusMonths(1)
                Pair(lastMonth.withDayOfMonth(1), lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()))
            }
            ReportPeriod.ALL_TIME -> Pair(LocalDate.of(2000, 1, 1), LocalDate.of(2099, 12, 31))
        }

        val startStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val endStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val filteredWorks = works.filter { it.date in startStr..endStr }
        val filteredPays = pays.filter { it.date in startStr..endStr }
        val filteredExps = exps.filter { it.date in startStr..endStr }

        var att = 0
        var earned = 0L
        for (w in filteredWorks) {
            att += w.attendanceUnits
            earned += w.earnedInPoisha
        }
        val received = filteredPays.sumOf { it.amountInPoisha }

        var food = 0L
        var other = 0L
        var duesDeduction = 0L
        for (e in filteredExps) {
            if (e.category == "খাবার") {
                food += e.amountInPoisha
            } else {
                other += e.amountInPoisha
            }
            if (e.isDeductedFromDues) {
                duesDeduction += e.amountInPoisha
            }
        }

        return FinancialSummary(
            totalAttendanceUnits = att,
            totalEarnedPoisha = earned,
            totalReceivedPoisha = received,
            totalExpensePoisha = food + other,
            foodExpensePoisha = food,
            otherExpensePoisha = other,
            deductedFromDuesPoisha = duesDeduction
        )
    }

    private fun calculateMonthlySummary(
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
            monthExpensePoisha = monthExpense,
            monthFoodExpensePoisha = monthFood,
            monthOtherExpensePoisha = monthOther,
            monthDeductedFromDuesPoisha = monthDuesDeduction
        )
    }
}
