package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.PaymentDao
import com.example.data.local.dao.WorkEntryDao
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.WorkEntryEntity

@Database(
    entities = [
        WorkEntryEntity::class,
        PaymentEntity::class,
        ExpenseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workEntryDao(): WorkEntryDao
    abstract fun paymentDao(): PaymentDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "worker_ledger_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
