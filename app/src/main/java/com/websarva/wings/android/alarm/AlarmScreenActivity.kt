package com.websarva.wings.android.alarm

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AlarmScreenActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ロック状態でも画面を表示する設定
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        // さらに必要なら Window フラグを追加
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        setContentView(R.layout.activity_alarm_screen)

        // 呼び出し元（例：AlarmReceiver）から渡されたExtrasを取得
        val alarmMusicUriString = intent.getStringExtra("alarmMusic")
        Log.d("AlarmScreenActivity", "Received alarmMusic URI: $alarmMusicUriString")

        // 指定された音源のURIがあればそれを、なければデフォルトの音源を再生
        mediaPlayer = if (!alarmMusicUriString.isNullOrEmpty()) {
            try {
                MediaPlayer.create(this, Uri.parse(alarmMusicUriString))
            } catch (e: Exception) {
                Log.e("AlarmScreenActivity", "Error creating MediaPlayer from URI", e)
                MediaPlayer.create(this, R.raw.alarm)
            }
        } else {
            MediaPlayer.create(this, R.raw.alarm)
        }

        mediaPlayer?.apply {
            isLooping = true  // 必要に応じてループ再生
            start()
        }

        // 停止ボタンの処理
        findViewById<Button>(R.id.btnStopAlarm).setOnClickListener {
            stopAlarm()
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        finish() // アラーム停止後にこの画面を閉じる
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

}
