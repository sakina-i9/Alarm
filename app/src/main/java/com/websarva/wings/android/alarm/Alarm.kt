package com.websarva.wings.android.alarm

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val time: String,
    val days: String,      // 例："日,月,火"
    val alarmName: String,
    val alarmMusic: String,
    val afterMusic: String
)
