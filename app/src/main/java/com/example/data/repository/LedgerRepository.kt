package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.DataStoreManager
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.WorkEntryEntity
import com.example.domain.model.CurrencyFormatter
import com.example.domain.model.DailySummary
import com.example.domain.model.FinancialSummary
import com.example.domain.model.RecentActivityItem
import com.example.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class LedgerRepository(
    private val database: AppDatabase,
    val dataStoreManager: DataStoreManager
) {
    private val workDao = database.workEntryDao()
    private val paymentDao = database.paymentDao()
    private val expenseDao = database.expenseDao()

    // --- Work Entries ---
    fun getAllWorkEntriesFlow(): Flow<List<WorkEntryEntity>> = workDao.getAllFlow()
    fun getWorkEntriesForDateFlow(date: String): Flow<List<WorkEntryEntity>> = workDao.getForDateFlow(date)
    fun getWorkEntriesForRangeFlow(startDate: String, endDate: String): Flow<List<WorkEntryEntity>> =
        workDao.getForDateRangeFlow(startDate, endDate)

    suspend fun getWorkEntryById(id: Long): WorkEntryEntity? = workDao.getById(id)
    suspend fun saveWorkEntry(entry: WorkEntryEntity): Long {
        return if (entry.id == 0L) {
            workDao.insert(entry)
        } else {
            workDao.update(entry.copy(updatedAt = System.currentTimeMillis()))
            entry.id
        }
    }
    suspend fun deleteWorkEntry(entry: WorkEntryEntity) = workDao.delete(entry)

    // --- Payments ---
    fun getAllPaymentsFlow(): Flow<List<PaymentEntity>> = paymentDao.getAllFlow()
    fun getPaymentsForDateFlow(date: String): Flow<List<PaymentEntity>> = paymentDao.getForDateFlow(date)
    fun getPaymentsForRangeFlow(startDate: String, endDate: String): Flow<List<PaymentEntity>> =
        paymentDao.getForDateRangeFlow(startDate, endDate)

    suspend fun getPaymentById(id: Long): PaymentEntity? = paymentDao.getById(id)
    suspend fun savePayment(payment: PaymentEntity): Long {
        return if (payment.id == 0L) {
            paymentDao.insert(payment)
        } else {
            paymentDao.update(payment.copy(updatedAt = System.currentTimeMillis()))
            payment.id
        }
    }
    suspend fun deletePayment(payment: PaymentEntity) = paymentDao.delete(payment)

    // --- Expenses ---
    fun getAllExpensesFlow(): Flow<List<ExpenseEntity>> = expenseDao.getAllFlow()
    fun getExpensesForDateFlow(date: String): Flow<List<ExpenseEntity>> = expenseDao.getForDateFlow(date)
    fun getExpensesForRangeFlow(startDate: String, endDate: String): Flow<List<ExpenseEntity>> =
        expenseDao.getForDateRangeFlow(startDate, endDate)

    suspend fun getExpenseById(id: Long): ExpenseEntity? = expenseDao.getById(id)
    suspend fun saveExpense(expense: ExpenseEntity): Long {
        return if (expense.id == 0L) {
            expenseDao.insert(expense)
        } else {
            expenseDao.update(expense.copy(updatedAt = System.currentTimeMillis()))
            expense.id
        }
    }
    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.delete(expense)

    // --- Overall Financial Summary ---
    fun getOverallSummaryFlow(): Flow<FinancialSummary> {
        return combine(
            workDao.getAllFlow(),
            paymentDao.getAllFlow(),
            expenseDao.getAllFlow()
        ) { works, payments, expenses ->
            calculateSummary(works, payments, expenses)
        }
    }

    // --- Today's Summary ---
    fun getTodaySummaryFlow(): Flow<DailySummary> {
        val today = CurrencyFormatter.todayIso()
        return combine(
            workDao.getForDateFlow(today),
            paymentDao.getForDateFlow(today),
            expenseDao.getForDateFlow(today)
        ) { works, payments, expenses ->
            var att = 0
            var earned = 0L
            for (w in works) {
                att += w.attendanceUnits
                earned += w.earnedInPoisha
            }
            val received = payments.sumOf { it.amountInPoisha }
            val expense = expenses.sumOf { it.amountInPoisha }
            DailySummary(
                date = today,
                attendanceUnits = att,
                earnedPoisha = earned,
                receivedPoisha = received,
                expensePoisha = expense
            )
        }
    }

    // --- Period Summary ---
    fun getPeriodSummaryFlow(startDate: String, endDate: String): Flow<FinancialSummary> {
        return combine(
            workDao.getForDateRangeFlow(startDate, endDate),
            paymentDao.getForDateRangeFlow(startDate, endDate),
            expenseDao.getForDateRangeFlow(startDate, endDate)
        ) { works, payments, expenses ->
            calculateSummary(works, payments, expenses)
        }
    }

    private fun calculateSummary(
        works: List<WorkEntryEntity>,
        payments: List<PaymentEntity>,
        expenses: List<ExpenseEntity>
    ): FinancialSummary {
        var totalAtt = 0
        var totalEarned = 0L
        for (w in works) {
            totalAtt += w.attendanceUnits
            totalEarned += w.earnedInPoisha
        }
        val totalReceived = payments.sumOf { it.amountInPoisha }

        var foodExpense = 0L
        var otherExpense = 0L
        for (e in expenses) {
            if (e.category == "খাবার") {
                foodExpense += e.amountInPoisha
            } else {
                otherExpense += e.amountInPoisha
            }
        }
        val totalExpense = foodExpense + otherExpense

        return FinancialSummary(
            totalAttendanceUnits = totalAtt,
            totalEarnedPoisha = totalEarned,
            totalReceivedPoisha = totalReceived,
            totalExpensePoisha = totalExpense,
            foodExpensePoisha = foodExpense,
            otherExpensePoisha = otherExpense
        )
    }

    // --- Recent Activities ---
    fun getRecentActivitiesFlow(limit: Int = 15): Flow<List<RecentActivityItem>> {
        return combine(
            workDao.getAllFlow(),
            paymentDao.getAllFlow(),
            expenseDao.getAllFlow()
        ) { works, payments, expenses ->
            val list = mutableListOf<RecentActivityItem>()

            for (w in works) {
                val attStr = CurrencyFormatter.formatAttendance(w.attendanceUnits)
                list.add(
                    RecentActivityItem(
                        id = w.id,
                        type = TransactionType.WORK,
                        date = w.date,
                        title = "হাজিরা: $attStr (${w.workType})",
                        subtitle = if (w.note.isNotBlank()) w.note else "রেট: ${CurrencyFormatter.formatTaka(w.rateInPoisha)}",
                        amountPoisha = w.earnedInPoisha,
                        timestamp = w.createdAt
                    )
                )
            }

            for (p in payments) {
                val source = if (p.payer.isNotBlank()) "মালিক: ${p.payer}" else "টাকা আদায়"
                list.add(
                    RecentActivityItem(
                        id = p.id,
                        type = TransactionType.PAYMENT,
                        date = p.date,
                        title = source,
                        subtitle = if (p.note.isNotBlank()) p.note else "টাকা পরিশোধ পেয়েছেন",
                        amountPoisha = p.amountInPoisha,
                        timestamp = p.createdAt
                    )
                )
            }

            for (e in expenses) {
                list.add(
                    RecentActivityItem(
                        id = e.id,
                        type = TransactionType.EXPENSE,
                        date = e.date,
                        title = "খরচ: ${e.category}",
                        subtitle = if (e.note.isNotBlank()) e.note else "ব্যক্তিগত খরচ",
                        amountPoisha = e.amountInPoisha,
                        timestamp = e.createdAt
                    )
                )
            }

            list.sortedWith(
                compareByDescending<RecentActivityItem> { it.date }
                    .thenByDescending { it.timestamp }
            ).take(limit)
        }
    }

    // --- Clear Data ---
    suspend fun clearAllData() {
        workDao.deleteAll()
        paymentDao.deleteAll()
        expenseDao.deleteAll()
    }

    // --- Load Demo Data ---
    suspend fun loadDemoData() {
        clearAllData()
        val today = LocalDate.now()
        val d1 = today.minusDays(3).toString()
        val d2 = today.minusDays(2).toString()
        val d3 = today.minusDays(1).toString()
        val d4 = today.toString()

        val defaultRate = 100000L // ৳1,000

        // 1.5 হাজিরা on d1
        workDao.insert(
            WorkEntryEntity(
                date = d1,
                attendanceUnits = 1500,
                rateInPoisha = defaultRate,
                earnedInPoisha = CurrencyFormatter.calculateEarnedPoisha(1500, defaultRate),
                workType = "পূর্ণ দিনের কাজ",
                note = "সকাল থেকে সন্ধ্যা অতিরিক্ত কাজ সহ"
            )
        )

        // 2 হাজিরা on d2
        workDao.insert(
            WorkEntryEntity(
                date = d2,
                attendanceUnits = 2000,
                rateInPoisha = defaultRate,
                earnedInPoisha = CurrencyFormatter.calculateEarnedPoisha(2000, defaultRate),
                workType = "চুক্তির কাজ",
                note = "দুই দিনের চুক্তিভিত্তিক কাজ একদিনে সম্পন্ন"
            )
        )

        // 1 হাজিরা on d3
        workDao.insert(
            WorkEntryEntity(
                date = d3,
                attendanceUnits = 1000,
                rateInPoisha = defaultRate,
                earnedInPoisha = CurrencyFormatter.calculateEarnedPoisha(1000, defaultRate),
                workType = "পূর্ণ দিনের কাজ",
                note = "স্বাভাবিক কাজ"
            )
        )

        // 0.5 হাজিরা today
        workDao.insert(
            WorkEntryEntity(
                date = d4,
                attendanceUnits = 500,
                rateInPoisha = defaultRate,
                earnedInPoisha = CurrencyFormatter.calculateEarnedPoisha(500, defaultRate),
                workType = "অর্ধ দিনের কাজ",
                note = "দুপুর পর্যন্ত হাফ বেলা কাজ"
            )
        )

        // Payments: ৳2,000 on d2, ৳1,000 on d4
        paymentDao.insert(
            PaymentEntity(
                date = d2,
                amountInPoisha = 200000L,
                payer = "রহিম সাহেব",
                note = "সাপ্তাহিক কিস্তির নগদ টাকা"
            )
        )
        paymentDao.insert(
            PaymentEntity(
                date = d4,
                amountInPoisha = 100000L,
                payer = "রহিম সাহেব",
                note = "হাতের খরচ বাবদ দেওয়া হয়েছে"
            )
        )

        // Expenses:
        expenseDao.insert(
            ExpenseEntity(
                date = d1,
                amountInPoisha = 12000L, // ৳120
                category = "খাবার",
                note = "দুপুরের খাবার ও চা-নাস্তা"
            )
        )
        expenseDao.insert(
            ExpenseEntity(
                date = d2,
                amountInPoisha = 15000L, // ৳150
                category = "খাবার",
                note = "হোটেলে দুপুরের ভাত"
            )
        )
        expenseDao.insert(
            ExpenseEntity(
                date = d3,
                amountInPoisha = 8000L, // ৳80
                category = "অন্যান্য",
                note = "কাজের প্রয়োজনীয় গ্লাভস ও সরঞ্জাম"
            )
        )
        expenseDao.insert(
            ExpenseEntity(
                date = d4,
                amountInPoisha = 6000L, // ৳60
                category = "খাবার",
                note = "চা বিস্কুট"
            )
        )
    }

    // --- JSON Backup Export ---
    suspend fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "Hazira Hisab")
        root.put("exportedAt", System.currentTimeMillis())

        val works = workDao.getAllList()
        val worksArray = JSONArray()
        for (w in works) {
            val obj = JSONObject()
            obj.put("date", w.date)
            obj.put("attendanceUnits", w.attendanceUnits)
            obj.put("rateInPoisha", w.rateInPoisha)
            obj.put("earnedInPoisha", w.earnedInPoisha)
            obj.put("workType", w.workType)
            obj.put("note", w.note)
            worksArray.put(obj)
        }
        root.put("works", worksArray)

        val payments = paymentDao.getAllList()
        val paymentsArray = JSONArray()
        for (p in payments) {
            val obj = JSONObject()
            obj.put("date", p.date)
            obj.put("amountInPoisha", p.amountInPoisha)
            obj.put("payer", p.payer)
            obj.put("note", p.note)
            paymentsArray.put(obj)
        }
        root.put("payments", paymentsArray)

        val expenses = expenseDao.getAllList()
        val expensesArray = JSONArray()
        for (e in expenses) {
            val obj = JSONObject()
            obj.put("date", e.date)
            obj.put("amountInPoisha", e.amountInPoisha)
            obj.put("category", e.category)
            obj.put("note", e.note)
            expensesArray.put(obj)
        }
        root.put("expenses", expensesArray)

        return root.toString(2)
    }

    // --- JSON Backup Restore ---
    suspend fun restoreBackupJson(jsonString: String, replaceExisting: Boolean = true): Result<Int> {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("works") || !root.has("payments") || !root.has("expenses")) {
                return Result.failure(IllegalArgumentException("ফাইলের ফরম্যাট সঠিক নয়। এটি হাজিরা হিসাবের ব্যাকআপ ফাইল নয়।"))
            }

            if (replaceExisting) {
                clearAllData()
            }

            var count = 0
            val worksArray = root.getJSONArray("works")
            val workList = mutableListOf<WorkEntryEntity>()
            for (i in 0 until worksArray.length()) {
                val obj = worksArray.getJSONObject(i)
                workList.add(
                    WorkEntryEntity(
                        date = obj.optString("date", CurrencyFormatter.todayIso()),
                        attendanceUnits = obj.optInt("attendanceUnits", 1000),
                        rateInPoisha = obj.optLong("rateInPoisha", 100000L),
                        earnedInPoisha = obj.optLong("earnedInPoisha", 100000L),
                        workType = obj.optString("workType", "পূর্ণ দিনের কাজ"),
                        note = obj.optString("note", "")
                    )
                )
                count++
            }
            if (workList.isNotEmpty()) {
                workDao.insertAll(workList)
            }

            val paymentsArray = root.getJSONArray("payments")
            val paymentList = mutableListOf<PaymentEntity>()
            for (i in 0 until paymentsArray.length()) {
                val obj = paymentsArray.getJSONObject(i)
                paymentList.add(
                    PaymentEntity(
                        date = obj.optString("date", CurrencyFormatter.todayIso()),
                        amountInPoisha = obj.optLong("amountInPoisha", 0L),
                        payer = obj.optString("payer", ""),
                        note = obj.optString("note", "")
                    )
                )
                count++
            }
            if (paymentList.isNotEmpty()) {
                paymentDao.insertAll(paymentList)
            }

            val expensesArray = root.getJSONArray("expenses")
            val expenseList = mutableListOf<ExpenseEntity>()
            for (i in 0 until expensesArray.length()) {
                val obj = expensesArray.getJSONObject(i)
                expenseList.add(
                    ExpenseEntity(
                        date = obj.optString("date", CurrencyFormatter.todayIso()),
                        amountInPoisha = obj.optLong("amountInPoisha", 0L),
                        category = obj.optString("category", "খাবার"),
                        note = obj.optString("note", "")
                    )
                )
                count++
            }
            if (expenseList.isNotEmpty()) {
                expenseDao.insertAll(expenseList)
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
