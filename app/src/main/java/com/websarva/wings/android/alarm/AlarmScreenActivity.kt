package com.websarva.wings.android.alarm

import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.yalantis.ucrop.BuildConfig
import java.io.File

class AlarmScreenActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ロック状態でも画面を表示する設定
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
        setContentView(R.layout.activity_alarm_screen)

        val ivAlarmBackground = findViewById<ImageView>(R.id.ivAlarmBackground)

        // Intent から背景画像のURIが渡されている場合の処理
        val backgroundUriString = intent.getStringExtra("backgroundUri")
        Log.d("AlarmScreenActivity", "Received backgroundUri: $backgroundUriString")

        if (!backgroundUriString.isNullOrEmpty() && backgroundUriString.startsWith("content://")) {
            try {
                val backgroundUri = Uri.parse(backgroundUriString)
                // ※ Drawable に変換してから設定する方法
                val inputStream = contentResolver.openInputStream(backgroundUri)
                val drawable: Drawable? = Drawable.createFromStream(inputStream, null)
                ivAlarmBackground.setImageDrawable(drawable)
                Log.d("AlarmScreenActivity", "背景画像を設定しました")
            } catch (e: Exception) {
                Log.e("AlarmScreenActivity", "背景画像の設定に失敗", e)
            }
        } else {
            // Intent からのURIが取得できない場合は、FileProviderを利用してキャッシュ内の画像を利用する
            try {
                val imageFile = File(cacheDir, "croppedImage.jpg")
                val contentUri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    imageFile
                )
                // ※ setImageURI で直接設定する例
                ivAlarmBackground.setImageURI(contentUri)
                Log.d("AlarmScreenActivity", "背景画像（FileProvider）を設定しました")
            } catch (e: Exception) {
                Log.e("AlarmScreenActivity", "FileProvider経由での背景画像の設定に失敗", e)
            }
        }

        // アラーム音の再生処理
        val alarmMusicUriString = intent.getStringExtra("alarmMusic")
        Log.d("AlarmScreenActivity", "Received alarmMusic URI: $alarmMusicUriString")
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
            isLooping = true
            start()
        }

        // 振動開始（振動機能は既に実装済みの startPersistentVibration() を呼び出す）
        startPersistentVibration(this)

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

        // 振動停止（既に実装済みの stopVibration() を呼び出す）
        stopVibration(this)

        // afterMusic の再生や次画面への遷移（例として CalendarActivity へ遷移）
        val afterMusicUriString = intent.getStringExtra("afterMusic")
        val calendarIntent = Intent(this, CalendarActivity::class.java)
        if (!afterMusicUriString.isNullOrEmpty()) {
            calendarIntent.putExtra("afterMusic", afterMusicUriString)
        }
        startActivity(calendarIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        stopVibration(this)
    }
}
