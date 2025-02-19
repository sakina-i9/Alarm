package com.websarva.wings.android.alarm

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AlarmDao {
    @Insert
    suspend fun insert(alarm: Alarm)

    @Query("SELECT * FROM alarms")
    suspend fun getAllAlarms(): List<Alarm>
}
