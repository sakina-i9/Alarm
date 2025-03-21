package com.websarva.wings.android.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.websarva.wings.android.alarm.AlarmUtils  // AlarmUtils をインポート

class AlarmActivity : AppCompatActivity() {

    // Room データベースの初期化
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
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
    }

    private lateinit var adapter: AlarmAdapter
    private var isSelectionMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alarm)

        // システムバーの余白設定
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Alarm)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 新規追加ボタン（＋）の設定
        val addAlarmButton = findViewById<ImageButton>(R.id.add_alarm)
        addAlarmButton.setOnClickListener {
            val intent = Intent(this, TimesetActivity::class.java)
            startActivity(intent)
        }

        // 戻るボタンの設定
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        }

        // RecyclerView の設定
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAlarms)
        //LinearLayoutManager を使うことで、縦方向にアイテムを並べて表示
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 削除ボタンや削除確認・キャンセルボタンの取得
        val deleteButton = findViewById<ImageButton>(R.id.alarm_delete)
        val confirmDeleteButton = findViewById<Button>(R.id.btnConfirmDelete)
        val cancelDeleteButton = findViewById<Button>(R.id.btnCancelDelete)
        //visibilityは表示状態を管理
        confirmDeleteButton.visibility = Button.GONE
        cancelDeleteButton.visibility = Button.GONE

        // Room から Alarm リストを取得して RecyclerView にセット
        CoroutineScope(Dispatchers.IO).launch {
            val alarms = db.alarmDao().getAllAlarms()
            runOnUiThread {
                adapter = AlarmAdapter(
                    alarms,
                    onEditClick = { alarm ->
                        if (!isSelectionMode) {
                            val intent = Intent(this@AlarmActivity, TimesetActivity::class.java).apply {
                                putExtra("alarmId", alarm.id)
                                putExtra("time", alarm.time)
                                putExtra("days", alarm.days)
                                putExtra("alarmName", alarm.alarmName)
                                putExtra("alarmMusic", alarm.alarmMusic)
                                putExtra("afterMusic", alarm.afterMusic)
                                putExtra("backgroundUri", alarm.backgroundUri)
                                putExtra("mode", "edit")
                                putExtra("enabled", alarm.enabled)
                            }
                            startActivity(intent)
                        }
                    },
                    onToggleChange = { alarm, isChecked ->
                        val updatedAlarm = alarm.copy(enabled = isChecked)
                        CoroutineScope(Dispatchers.IO).launch {
                            db.alarmDao().update(updatedAlarm)
                            if (isChecked) {
                                // ON の場合：必要に応じて再スケジュールする処理
                                val targetTimeInMillis = AlarmUtils.getTargetTimeInMillisFrom(alarm.time)
                                AlarmUtils.scheduleAlarm(this@AlarmActivity, targetTimeInMillis, updatedAlarm)
                            } else {
                                // OFF の場合：PendingIntent をキャンセルしてアラーム停止
                                val intent = Intent(this@AlarmActivity, AlarmReceiver::class.java).apply {
                                    putExtra("alarmId", updatedAlarm.id)
                                }
                                val pendingIntent = PendingIntent.getBroadcast(
                                    this@AlarmActivity,
                                    updatedAlarm.id,
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )
                                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                alarmManager.cancel(pendingIntent)
                            }
                        }
                    }
                )
                recyclerView.adapter = adapter
            }
        }

        // 削除ボタンの処理：選択モードに切り替え、削除確認とキャンセルボタンを表示
        deleteButton.setOnClickListener {
            if (!isSelectionMode) {
                isSelectionMode = true
                adapter.selectionMode = true
                Toast.makeText(this, "削除する項目を選択してください", Toast.LENGTH_SHORT).show()
                confirmDeleteButton.visibility = Button.VISIBLE
                cancelDeleteButton.visibility = Button.VISIBLE
            }
        }

        // 削除確認ボタンの処理：選択されたアラームを削除
        confirmDeleteButton.setOnClickListener {
            val selectedAlarms = adapter.getSelectedAlarms()
            if (selectedAlarms.isEmpty()) {
                Toast.makeText(this, "削除する項目を選択してください", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    selectedAlarms.forEach { alarm ->
                        db.alarmDao().delete(alarm)
                    }
                    val updatedAlarms = db.alarmDao().getAllAlarms()
                    runOnUiThread {
                        adapter.selectionMode = false
                        isSelectionMode = false
                        adapter.clearSelection()
                        adapter.updateAlarms(updatedAlarms)
                        Toast.makeText(this@AlarmActivity, "選択されたアラームを削除しました", Toast.LENGTH_SHORT).show()
                        confirmDeleteButton.visibility = Button.GONE
                        cancelDeleteButton.visibility = Button.GONE
                    }
                }
            }
        }

        // キャンセルボタンの処理：削除モードをキャンセルして通常状態に戻す
        cancelDeleteButton.setOnClickListener {
            adapter.selectionMode = false
            isSelectionMode = false
            adapter.clearSelection()
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "キャンセルしました", Toast.LENGTH_SHORT).show()
            confirmDeleteButton.visibility = Button.GONE
            cancelDeleteButton.visibility = Button.GONE
        }
    }
}
