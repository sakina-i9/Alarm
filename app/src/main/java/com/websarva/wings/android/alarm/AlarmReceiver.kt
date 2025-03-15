package com.websarva.wings.android.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        // WakeLock を static に保持
        private var wakeLock: PowerManager.WakeLock? = null

        // 他からも解放できるようにリリース用メソッドを用意
        fun releaseWakeLock() {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d("AlarmReceiver", "WakeLock released")
                }
                wakeLock = null
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm triggered!")

        // 部分的な WakeLock を取得（タイムアウト付きで安全策）
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp:AlarmWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L) // 10分後に自動解放（必要に応じて調整）
        Log.d("AlarmReceiver", "WakeLock acquired")

        // アラーム画面（AlarmScreenActivity）を起動
        val alarmIntent = Intent(context, AlarmScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("alarmMusic", intent.getStringExtra("alarmMusic"))
            putExtra("afterMusic", intent.getStringExtra("afterMusic"))
            putExtra("backgroundUri", intent.getStringExtra("backgroundUri"))
        }
        context.startActivity(alarmIntent)
    }
}
