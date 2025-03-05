package com.websarva.wings.android.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm triggered!")
        // AlarmScreenActivity を直接起動する
        val alarmIntent = Intent(context, AlarmScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("alarmMusic", intent.getStringExtra("alarmMusic"))
            putExtra("afterMusic", intent.getStringExtra("afterMusic"))
        }
        context.startActivity(alarmIntent)
    }
}

