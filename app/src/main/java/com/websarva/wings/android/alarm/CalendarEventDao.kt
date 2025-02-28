package com.websarva.wings.android.alarm

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events")
    suspend fun getAllEvents(): List<CalendarEvent>

    @Insert
    suspend fun insertEvent(event: CalendarEvent)

    @Update
    suspend fun updateEvent(event: CalendarEvent)

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)
}
