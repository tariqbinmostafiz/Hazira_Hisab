package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.WorkEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkEntryDao {

    @Query("SELECT * FROM work_entries ORDER BY date DESC, id DESC")
    fun getAllFlow(): Flow<List<WorkEntryEntity>>

    @Query("SELECT * FROM work_entries WHERE date = :date ORDER BY id DESC")
    fun getForDateFlow(date: String): Flow<List<WorkEntryEntity>>

    @Query("SELECT * FROM work_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, id DESC")
    fun getForDateRangeFlow(startDate: String, endDate: String): Flow<List<WorkEntryEntity>>

    @Query("SELECT * FROM work_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WorkEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WorkEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<WorkEntryEntity>)

    @Update
    suspend fun update(entry: WorkEntryEntity)

    @Delete
    suspend fun delete(entry: WorkEntryEntity)

    @Query("DELETE FROM work_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM work_entries")
    suspend fun deleteAll()

    @Query("SELECT * FROM work_entries")
    suspend fun getAllList(): List<WorkEntryEntity>
}
