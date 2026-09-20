package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_entries")
data class WorkEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: String, // ISO YYYY-MM-DD
    val attendanceUnits: Int, // 1000 = 1.0 হাজিরা, 500 = 0.5 হাজিরা
    val rateInPoisha: Long, // e.g. 100000L for ৳1000
    val earnedInPoisha: Long, // calculated at save time = (attendanceUnits * rateInPoisha) / 1000
    val workType: String = "পূর্ণ দিনের কাজ", // e.g. পূর্ণ দিন, অর্ধ দিন, চুক্তি, ইত্যাদি
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
