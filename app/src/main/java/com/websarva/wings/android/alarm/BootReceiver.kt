package com.websarva.wings.android.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted. Rescheduling alarms.")

            // Room データベースのインスタンスを生成
            val db = Room.databaseBuilder(
                context,
                AlarmDatabase::class.java,
                "alarms-db"
            )
                .addMigrations(
                    AlarmDatabase.MIGRATION_1_2,
                    AlarmDatabase.MIGRATION_2_3,
                    AlarmDatabase.MIGRATION_3_4,
                    AlarmDatabase.MIGRATION_4_5
                )
                .build()

            // コルーチンを使用して suspend 関数を呼び出す
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val alarms = db.alarmDao().getAllAlarms() // suspend 関数をコルーチン内で呼び出す
                    for (alarm in alarms) {
                        if (alarm.enabled) {
                            val targetTimeInMillis = calculateTargetTimeInMillis(alarm.time)
                            // 対象時刻が未来の場合のみスケジュール
                            if (targetTimeInMillis > System.currentTimeMillis()) {
                                scheduleAlarm(context, targetTimeInMillis, alarm)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling alarms", e)
                } finally {
                    db.close()
                }
            }
        }
    }

    // alarm.time（例："午前 08:30" や "午後 07:45"）からミリ秒を計算する例
    private fun calculateTargetTimeInMillis(timeText: String): Long {
        val parts = timeText.split(" ")
        if (parts.size < 2) return System.currentTimeMillis()
        val amPm = parts[0]
        val timeParts = parts[1].split(":")
        if (timeParts.size < 2) return System.currentTimeMillis()
        var hour = timeParts[0].toIntOrNull() ?: 0
        val minute = timeParts[1].toIntOrNull() ?: 0
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
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return calendar.timeInMillis
    }

    // AlarmReceiver を用いてアラームをスケジュールする処理
    private fun scheduleAlarm(context: Context, timeInMillis: Long, alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarmId", alarm.id)
            putExtra("alarmMusic", alarm.alarmMusic)
            putExtra("afterMusic", alarm.afterMusic)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                } else {
                    // ユーザーに設定画面へ遷移させるなどの処理
                    val settingsIntent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    settingsIntent.data = Uri.parse("package:" + context.packageName)
                    context.startActivity(settingsIntent)
                }
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            }
        } else {
            Log.e("BootReceiver", "AlarmManager is null")
        }
    }
}
