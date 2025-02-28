package com.websarva.wings.android.alarm

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // 日付は "yyyy-MM-dd" 形式で保存する（例："2025-03-15"）
    val date: String,
    val schedule: String
)
