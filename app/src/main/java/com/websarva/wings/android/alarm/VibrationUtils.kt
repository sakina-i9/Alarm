package com.websarva.wings.android.alarm

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator


fun startPersistentVibration(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    vibrator?.let {
        // パターン例: 即時開始 → 1000ms振動 → 500ms停止 → 繰り返し
        val pattern = longArrayOf(0, 1000, 500)
        val repeatIndex = 0 // 0番目から繰り返す
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, repeatIndex)
            it.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            it.vibrate(pattern, repeatIndex)
        }
    }
}

fun stopVibration(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    vibrator?.cancel()
}
