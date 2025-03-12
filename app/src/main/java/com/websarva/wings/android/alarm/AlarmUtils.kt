package com.websarva.wings.android.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.util.*

object AlarmUtils {

    fun getTargetTimeInMillisFrom(timeText: String): Long {
        // 例: "午前 08:30" の形式を想定
        val parts = timeText.split(" ")
        if (parts.size < 2) return System.currentTimeMillis()
        val amPm = parts[0]
        val timeParts = parts[1].split(":")
        if (timeParts.size < 2) return System.currentTimeMillis()
        var hour = timeParts[0].toIntOrNull() ?: 0
        val minute = timeParts[1].toIntOrNull() ?: 0
        // 午前午後の調整
        if (amPm == "午後" && hour < 12) {
            hour += 12
        } else if (amPm == "午前" && hour == 12) {
            hour = 0
        }
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        // もし指定時刻がすでに過ぎていたら翌日に設定
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return calendar.timeInMillis
    }

    fun scheduleAlarm(context: Context, timeInMillis: Long, alarm: Alarm) {
        if (!alarm.enabled) {
            // アラームが無効なら何もしない
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarmId", alarm.id)
            putExtra("alarmMusic", alarm.alarmMusic)
            putExtra("afterMusic", alarm.afterMusic)
            putExtra("backgroundUri", alarm.backgroundUri)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (alarmManager.canScheduleExactAlarms()) {
                // 正確なアラームのスケジュールが許可されている場合
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            } else {
                // 正確なアラームのスケジュールが許可されていない場合は set() で代替（正確性は保証されない）
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                Toast.makeText(context, "正確なアラームの設定は許可されていません", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "アラームのスケジュールに失敗しました: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
