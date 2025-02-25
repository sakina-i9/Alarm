package com.websarva.wings.android.alarm

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AlarmDao {
    @Insert
    suspend fun insert(alarm: Alarm)

    @Update
    suspend fun update(alarm: Alarm)

    @Delete
    suspend fun  delete(alarm: Alarm)

    @Query("SELECT * FROM alarms")
    suspend fun getAllAlarms(): List<Alarm>

    @Query("SELECT * FROM alarms WHERE id = :alarmId LIMIT 1")
    suspend fun getAlarmById(alarmId: Int): Alarm?
}
