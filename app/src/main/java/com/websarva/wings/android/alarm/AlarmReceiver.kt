package com.websarva.wings.android.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm triggered!")
        // バイブレーションを開始
        startPersistentVibration(context)

        val alarmIntent = Intent(context, AlarmScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("alarmMusic", intent.getStringExtra("alarmMusic"))
            putExtra("afterMusic", intent.getStringExtra("afterMusic"))
            putExtra("backgroundUri", intent.getStringExtra("backgroundUri"))
        }
        context.startActivity(alarmIntent)
    }
}
