package com.shifat.hajirahisab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_entries")
data class WorkEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: String, // ISO YYYY-MM-DD
    val attendanceUnits: Int, // 1000 = 1 হাজিরা, 500 = হাফ হাজিরা, 1500 = ১.৫ হাজিরা
    val rateInPoisha: Long, // রেট (পয়সা)
    val earnedInPoisha: Long, // মোট আয় (পয়সা)
    val workType: String = "পূর্ণ দিনের কাজ", // হাজিরা ধরন
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
