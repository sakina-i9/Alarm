package com.websarva.wings.android.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm triggered!")
        // 直接音を再生するのではなく、AlarmScreenActivity を起動する
        val alarmIntent = Intent(context, AlarmScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // 必要に応じて Extras を渡す（例えば alarmMusic URI）
            putExtra("alarmMusic", intent.getStringExtra("alarmMusic"))
        }
        context.startActivity(alarmIntent)
    }
}
