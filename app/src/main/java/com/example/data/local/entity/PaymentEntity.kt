package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: String, // ISO YYYY-MM-DD
    val amountInPoisha: Long,
    val payer: String = "", // e.g. মালিক / ঠিকাদারের নাম
    val note: String = "",
    val paymentTarget: String = TARGET_CURRENT_MONTH, // CURRENT_MONTH or PREVIOUS_DUES
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TARGET_CURRENT_MONTH = "CURRENT_MONTH"
        const val TARGET_PREVIOUS_DUES = "PREVIOUS_DUES"
    }

    val isForPreviousDues: Boolean
        get() = paymentTarget == TARGET_PREVIOUS_DUES
}
