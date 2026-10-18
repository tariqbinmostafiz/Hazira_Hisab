package com.shifat.hajirahisab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: String, // ISO YYYY-MM-DD
    val amountInPoisha: Long,
    val category: String = "খাবার", // খাবার, যাতায়াত, চিকিৎসা, অন্যান্য
    val note: String = "",
    val paymentSource: String = SOURCE_CASH, // CASH or FROM_DUES
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val SOURCE_CASH = "CASH"
        const val SOURCE_FROM_DUES = "FROM_DUES"
    }

    val isDeductedFromDues: Boolean
        get() = paymentSource == SOURCE_FROM_DUES
}
