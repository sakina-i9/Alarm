package com.websarva.wings.android.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class AlarmForegroundService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var afterMediaPlayer: MediaPlayer? = null

    // ここで afterMusic の URI を保持する変数を追加
    private var afterMusicUriString: String? = null

    companion object {
        const val CHANNEL_ID = "alarm_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = buildNotification("アラーム", "アラームが鳴っています")
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // intent から alarmMusic, afterMusic を取得し、afterMusic はクラス変数に保持
        val alarmMusicUriString = intent?.getStringExtra("alarmMusic")
        afterMusicUriString = intent?.getStringExtra("afterMusic")
        Log.d("AlarmForegroundService", "alarmMusic URI: $alarmMusicUriString")
        Log.d("AlarmForegroundService", "afterMusic URI: $afterMusicUriString")

        // alarmMusic の再生
        if (!alarmMusicUriString.isNullOrEmpty()) {
            try {
                mediaPlayer = MediaPlayer.create(this, Uri.parse(alarmMusicUriString))
                mediaPlayer?.apply {
                    isLooping = true
                    start()
                }
            } catch (e: Exception) {
                Log.e("AlarmForegroundService", "Error playing alarm music", e)
            }
        } else {
            Log.d("AlarmForegroundService", "No alarmMusic URI provided")
        }

        // ※afterMusic の再生は、例えばユーザーの操作やアラーム停止時に playAfterMusic() を呼び出すことで実行する

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null

        afterMediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        afterMediaPlayer = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Alarm Channel"
            val channelDescription = "アラーム通知用のチャンネル"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = channelDescription
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, text: String): Notification {
        // AlarmScreenActivity を起動するための Intent と PendingIntent を作成
        val fullScreenIntent = Intent(this, AlarmScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.time) // 適宜アイコンに変更
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(fullScreenPendingIntent, true) // ここで fullScreenIntent を設定
            .build()
    }

    // アラーム停止時に afterMusic を再生するメソッド例
    fun playAfterMusic() {
        // alarmMusic を停止
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null

        // ここではクラス変数 afterMusicUriString を利用
        if (!afterMusicUriString.isNullOrEmpty()) {
            try {
                afterMediaPlayer = MediaPlayer.create(this, Uri.parse(afterMusicUriString))
                afterMediaPlayer?.apply {
                    isLooping = false
                    start()
                    setOnCompletionListener { mp ->
                        mp.release()
                        afterMediaPlayer = null
                        stopSelf() // afterMusic の再生完了後にサービス停止
                    }
                }
            } catch (e: Exception) {
                Log.e("AlarmForegroundService", "Error playing after music", e)
                stopSelf()
            }
        } else {
            stopSelf()
        }
    }
}
