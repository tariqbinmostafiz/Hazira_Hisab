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
import com.example.domain.model.RecentActivityItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
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
        note: String
    ) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                id = id,
                date = date,
                amountInPoisha = amountInPoisha,
                category = category,
                note = note
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
        for (e in filteredExps) {
            if (e.category == "খাবার") food += e.amountInPoisha else other += e.amountInPoisha
        }

        return FinancialSummary(
            totalAttendanceUnits = att,
            totalEarnedPoisha = earned,
            totalReceivedPoisha = received,
            totalExpensePoisha = food + other,
            foodExpensePoisha = food,
            otherExpensePoisha = other
        )
    }
}
