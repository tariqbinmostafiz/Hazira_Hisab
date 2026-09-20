package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments ORDER BY date DESC, id DESC")
    fun getAllFlow(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE date = :date ORDER BY id DESC")
    fun getForDateFlow(date: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, id DESC")
    fun getForDateRangeFlow(startDate: String, endDate: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<PaymentEntity>)

    @Update
    suspend fun update(payment: PaymentEntity)

    @Delete
    suspend fun delete(payment: PaymentEntity)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM payments")
    suspend fun deleteAll()

    @Query("SELECT * FROM payments")
    suspend fun getAllList(): List<PaymentEntity>
}
