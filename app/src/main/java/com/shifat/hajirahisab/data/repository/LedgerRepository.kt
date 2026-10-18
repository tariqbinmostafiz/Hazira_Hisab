package com.shifat.hajirahisab.data.repository

import android.content.Context
import com.shifat.hajirahisab.data.local.AppDatabase
import com.shifat.hajirahisab.data.local.dao.ExpenseDao
import com.shifat.hajirahisab.data.local.dao.PaymentDao
import com.shifat.hajirahisab.data.local.dao.WorkEntryDao
import com.shifat.hajirahisab.data.local.entity.ExpenseEntity
import com.shifat.hajirahisab.data.local.entity.PaymentEntity
import com.shifat.hajirahisab.data.local.entity.WorkEntryEntity
import com.shifat.hajirahisab.domain.model.CurrencyFormatter
import com.shifat.hajirahisab.domain.model.DailySummary
import com.shifat.hajirahisab.domain.model.FinancialSummary
import com.shifat.hajirahisab.domain.model.RecentActivityItem
import com.shifat.hajirahisab.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LedgerRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val workDao: WorkEntryDao = db.workEntryDao()
    private val paymentDao: PaymentDao = db.paymentDao()
    private val expenseDao: ExpenseDao = db.expenseDao()

    private val prefs = context.getSharedPreferences("worker_ledger_prefs", Context.MODE_PRIVATE)

    // --- Settings Preferences ---
    fun getDefaultRatePoisha(): Long = prefs.getLong("default_rate_poisha", 100000L) // Default ৳1000
    fun setDefaultRatePoisha(rate: Long) = prefs.edit().putLong("default_rate_poisha", rate).apply()

    fun getThemeMode(): String = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
    fun setThemeMode(mode: String) = prefs.edit().putString("theme_mode", mode).apply()

    fun getUseBengaliDigits(): Boolean = prefs.getBoolean("use_bengali_digits", true)
    fun setUseBengaliDigits(use: Boolean) = prefs.edit().putBoolean("use_bengali_digits", use).apply()

    // --- Raw Entity Flows ---
    fun getWorkEntriesFlow(): Flow<List<WorkEntryEntity>> = workDao.getAllFlow()
    fun getPaymentsFlow(): Flow<List<PaymentEntity>> = paymentDao.getAllFlow()
    fun getExpensesFlow(): Flow<List<ExpenseEntity>> = expenseDao.getAllFlow()

    suspend fun getAllWorks(): List<WorkEntryEntity> = withContext(Dispatchers.IO) { workDao.getAllList() }
    suspend fun getAllPayments(): List<PaymentEntity> = withContext(Dispatchers.IO) { paymentDao.getAllList() }
    suspend fun getAllExpenses(): List<ExpenseEntity> = withContext(Dispatchers.IO) { expenseDao.getAllList() }

    // --- Work Entry CRUD ---
    suspend fun saveWorkEntry(entry: WorkEntryEntity): Long = withContext(Dispatchers.IO) {
        if (entry.id == 0L) {
            workDao.insert(entry)
        } else {
            workDao.update(entry.copy(updatedAt = System.currentTimeMillis()))
            entry.id
        }
    }

    suspend fun deleteWorkEntry(entry: WorkEntryEntity) = withContext(Dispatchers.IO) {
        workDao.delete(entry)
    }

    // --- Payment CRUD ---
    suspend fun savePayment(payment: PaymentEntity): Long = withContext(Dispatchers.IO) {
        if (payment.id == 0L) {
            paymentDao.insert(payment)
        } else {
            paymentDao.update(payment.copy(updatedAt = System.currentTimeMillis()))
            payment.id
        }
    }

    suspend fun deletePayment(payment: PaymentEntity) = withContext(Dispatchers.IO) {
        paymentDao.delete(payment)
    }

    // --- Expense CRUD ---
    suspend fun saveExpense(expense: ExpenseEntity): Long = withContext(Dispatchers.IO) {
        if (expense.id == 0L) {
            expenseDao.insert(expense)
        } else {
            expenseDao.update(expense.copy(updatedAt = System.currentTimeMillis()))
            expense.id
        }
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.delete(expense)
    }

    // --- Clear All Data ---
    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        workDao.deleteAll()
        paymentDao.deleteAll()
        expenseDao.deleteAll()
    }

    // --- Recent Activities Flow (Unified, sorted by date DESC, timestamp DESC) ---
    fun getRecentActivitiesFlow(): Flow<List<RecentActivityItem>> {
        return combine(
            workDao.getAllFlow(),
            paymentDao.getAllFlow(),
            expenseDao.getAllFlow()
        ) { works, payments, expenses ->
            val list = mutableListOf<RecentActivityItem>()

            for (w in works) {
                val subtitleText = if (w.note.isNotBlank()) "${w.workType} • ${w.note}" else w.workType
                list.add(
                    RecentActivityItem(
                        id = w.id,
                        type = TransactionType.WORK,
                        date = w.date,
                        title = "কাজের হাজিরা (${CurrencyFormatter.formatAttendance(w.attendanceUnits)})",
                        subtitle = subtitleText,
                        amountPoisha = w.earnedInPoisha,
                        timestamp = w.createdAt
                    )
                )
            }

            for (p in payments) {
                val source = if (p.payer.isNotBlank()) "মালিক: ${p.payer}" else "টাকা আদায়"
                val subtitleText = when {
                    p.isForPreviousDues && p.note.isNotBlank() -> "পূর্বের বকেয়া পরিশোধ • ${p.note}"
                    p.isForPreviousDues -> "পূর্বের বকেয়া পরিশোধ বাবদ"
                    p.note.isNotBlank() -> p.note
                    else -> "টাকা পরিশোধ পেয়েছেন"
                }
                list.add(
                    RecentActivityItem(
                        id = p.id,
                        type = TransactionType.PAYMENT,
                        date = p.date,
                        title = source,
                        subtitle = subtitleText,
                        amountPoisha = p.amountInPoisha,
                        timestamp = p.createdAt
                    )
                )
            }

            for (e in expenses) {
                val expenseSub = if (e.isDeductedFromDues) {
                    if (e.note.isNotBlank()) "পাওনা টাকা থেকে কাটা • ${e.note}" else "পাওনা টাকা থেকে কাটা হয়েছে"
                } else {
                    if (e.note.isNotBlank()) e.note else "ব্যক্তিগত খরচ"
                }
                list.add(
                    RecentActivityItem(
                        id = e.id,
                        type = TransactionType.EXPENSE,
                        date = e.date,
                        title = "খরচ (${e.category})",
                        subtitle = expenseSub,
                        amountPoisha = e.amountInPoisha,
                        timestamp = e.createdAt
                    )
                )
            }

            list.sortWith(compareByDescending<RecentActivityItem> { it.date }.thenByDescending { it.timestamp })
            list.take(40)
        }
    }

    // --- Today's Summary Flow ---
    fun getTodaySummaryFlow(): Flow<DailySummary> {
        val todayStr = CurrencyFormatter.todayIso()
        return combine(
            workDao.getAllFlow(),
            paymentDao.getAllFlow(),
            expenseDao.getAllFlow()
        ) { works, payments, expenses ->
            var attendance = 0
            var earned = 0L
            var received = 0L
            var expense = 0L

            for (w in works) {
                if (w.date == todayStr) {
                    attendance += w.attendanceUnits
                    earned += w.earnedInPoisha
                }
            }
            for (p in payments) {
                if (p.date == todayStr) {
                    received += p.amountInPoisha
                }
            }
            for (e in expenses) {
                if (e.date == todayStr) {
                    expense += e.amountInPoisha
                }
            }

            DailySummary(
                date = todayStr,
                attendanceUnits = attendance,
                earnedPoisha = earned,
                receivedPoisha = received,
                expensePoisha = expense
            )
        }
    }

    // --- Overall Lifetime Summary Flow ---
    fun getOverallSummaryFlow(): Flow<FinancialSummary> {
        return combine(
            workDao.getAllFlow(),
            paymentDao.getAllFlow(),
            expenseDao.getAllFlow()
        ) { works, payments, expenses ->
            var attUnits = 0
            var earned = 0L
            var received = 0L
            var expense = 0L
            var food = 0L
            var other = 0L
            var duesDeduction = 0L

            for (w in works) {
                attUnits += w.attendanceUnits
                earned += w.earnedInPoisha
            }
            for (p in payments) {
                received += p.amountInPoisha
            }
            for (e in expenses) {
                expense += e.amountInPoisha
                if (e.category == "খাবার") {
                    food += e.amountInPoisha
                } else {
                    other += e.amountInPoisha
                }
                if (e.isDeductedFromDues) {
                    duesDeduction += e.amountInPoisha
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
                workCount = works.size,
                paymentCount = payments.size,
                expenseCount = expenses.size
            )
        }
    }

    // --- Filtered Period Summary Flow ---
    fun getPeriodSummaryFlow(startDate: String, endDate: String): Flow<FinancialSummary> {
        return combine(
            workDao.getAllFlow(),
            paymentDao.getAllFlow(),
            expenseDao.getAllFlow()
        ) { works, payments, expenses ->
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
                if (w.date in startDate..endDate) {
                    attUnits += w.attendanceUnits
                    earned += w.earnedInPoisha
                    wCount++
                }
            }
            for (p in payments) {
                if (p.date in startDate..endDate) {
                    received += p.amountInPoisha
                    pCount++
                }
            }
            for (e in expenses) {
                if (e.date in startDate..endDate) {
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
        }
    }

    // --- JSON Backup Export ---
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 2)
        root.put("appName", "Hajira Hisab")
        root.put("exportDate", CurrencyFormatter.todayIso())
        root.put("timestamp", System.currentTimeMillis())

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
            obj.put("paymentTarget", p.paymentTarget)
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
            obj.put("paymentSource", e.paymentSource)
            expensesArray.put(obj)
        }
        root.put("expenses", expensesArray)

        root.toString(2)
    }

    // --- JSON Backup Restore ---
    suspend fun restoreBackupJson(jsonString: String, replaceExisting: Boolean = true): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("works") || !root.has("payments") || !root.has("expenses")) {
                return@withContext Result.failure(IllegalArgumentException("ফাইলের ফরম্যাট সঠিক নয়। এটি হাজিরা হিসাবের ব্যাকআপ ফাইল নয়।"))
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
                        note = obj.optString("note", ""),
                        paymentTarget = obj.optString("paymentTarget", PaymentEntity.TARGET_CURRENT_MONTH)
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
                        note = obj.optString("note", ""),
                        paymentSource = obj.optString("paymentSource", ExpenseEntity.SOURCE_CASH)
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

    // --- Demo Data Loader ---
    suspend fun loadDemoData() = withContext(Dispatchers.IO) {
        clearAllData()

        val today = LocalDate.now()
        val dtf = DateTimeFormatter.ISO_LOCAL_DATE

        // Last month records
        val lastMonthDay5 = today.minusMonths(1).withDayOfMonth(5).format(dtf)
        val lastMonthDay10 = today.minusMonths(1).withDayOfMonth(10).format(dtf)
        val lastMonthDay15 = today.minusMonths(1).withDayOfMonth(15).format(dtf)
        val lastMonthDay20 = today.minusMonths(1).withDayOfMonth(20).format(dtf)

        workDao.insertAll(
            listOf(
                WorkEntryEntity(date = lastMonthDay5, attendanceUnits = 1000, rateInPoisha = 100000L, earnedInPoisha = 100000L, workType = "পূর্ণ দিনের কাজ"),
                WorkEntryEntity(date = lastMonthDay10, attendanceUnits = 1000, rateInPoisha = 100000L, earnedInPoisha = 100000L, workType = "পূর্ণ দিনের কাজ"),
                WorkEntryEntity(date = lastMonthDay15, attendanceUnits = 1000, rateInPoisha = 100000L, earnedInPoisha = 100000L, workType = "পূর্ণ দিনের কাজ"),
                WorkEntryEntity(date = lastMonthDay20, attendanceUnits = 1000, rateInPoisha = 100000L, earnedInPoisha = 100000L, workType = "পূর্ণ দিনের কাজ")
            )
        )

        paymentDao.insertAll(
            listOf(
                PaymentEntity(date = lastMonthDay15, amountInPoisha = 200000L, payer = "রহিম ভাই", note = "অর্ধেক টাকা পরিশোধ", paymentTarget = PaymentEntity.TARGET_CURRENT_MONTH)
            )
        )

        expenseDao.insertAll(
            listOf(
                ExpenseEntity(date = lastMonthDay10, amountInPoisha = 15000L, category = "খাবার", note = "দুপুরের খাবার"),
                ExpenseEntity(date = lastMonthDay20, amountInPoisha = 20000L, category = "অন্যান্য", note = "কাজের জুতো", paymentSource = ExpenseEntity.SOURCE_FROM_DUES)
            )
        )

        // Current month records
        val thisMonthDay2 = today.withDayOfMonth(kotlin.math.min(2, today.lengthOfMonth())).format(dtf)
        val thisMonthDay5 = today.withDayOfMonth(kotlin.math.min(5, today.lengthOfMonth())).format(dtf)
        val todayStr = today.format(dtf)

        workDao.insertAll(
            listOf(
                WorkEntryEntity(date = thisMonthDay2, attendanceUnits = 1000, rateInPoisha = 100000L, earnedInPoisha = 100000L, workType = "পূর্ণ দিনের কাজ"),
                WorkEntryEntity(date = thisMonthDay5, attendanceUnits = 1500, rateInPoisha = 100000L, earnedInPoisha = 150000L, workType = "ওভারটাইম কাজ"),
                WorkEntryEntity(date = todayStr, attendanceUnits = 1000, rateInPoisha = 100000L, earnedInPoisha = 100000L, workType = "পূর্ণ দিনের কাজ")
            )
        )

        paymentDao.insertAll(
            listOf(
                PaymentEntity(date = thisMonthDay5, amountInPoisha = 100000L, payer = "করিম ঠিকাদার", note = "চলতি সপ্তাহের নগদ টাকা", paymentTarget = PaymentEntity.TARGET_CURRENT_MONTH),
                PaymentEntity(date = thisMonthDay2, amountInPoisha = 100000L, payer = "রহিম ভাই", note = "আগের মাসের বকেয়া পরিশোধ", paymentTarget = PaymentEntity.TARGET_PREVIOUS_DUES)
            )
        )

        expenseDao.insertAll(
            listOf(
                ExpenseEntity(date = thisMonthDay2, amountInPoisha = 12000L, category = "খাবার", note = "নাস্তা ও চা"),
                ExpenseEntity(date = todayStr, amountInPoisha = 15000L, category = "খাবার", note = "দুপুরের খাবার")
            )
        )
    }
}
