package com.websarva.wings.android.alarm
import android.content.Intent
import android.os.Bundle
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

class AlarmActivity : AppCompatActivity() {

    // Room データベースをシングルトン的に生成
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AlarmDatabase::class.java,
            "alarms-db"
        ).build()
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

        // RecyclerView の設定
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAlarms)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 非同期で Room から全アラームを取得して RecyclerView にセット
        CoroutineScope(Dispatchers.IO).launch {
            val alarms = db.alarmDao().getAllAlarms()
            runOnUiThread {
                adapter = AlarmAdapter(alarms) { alarm ->
                    // 編集モードでない場合は、編集画面へ遷移
                    if (!isSelectionMode) {
                        val intent = Intent(this@AlarmActivity, TimesetActivity::class.java).apply {
                            putExtra("alarmId", alarm.id)
                            putExtra("time", alarm.time)
                            putExtra("days", alarm.days)
                            putExtra("alarmName", alarm.alarmName)
                            putExtra("alarmMusic", alarm.alarmMusic)
                            putExtra("afterMusic", alarm.afterMusic)
                            putExtra("mode", "edit")
                        }
                        startActivity(intent)
                    }
                }
                recyclerView.adapter = adapter
            }
        }

        // 削除ボタンの設定（複数選択削除用）
        val deleteButton = findViewById<ImageButton>(R.id.alarm_delete)
        deleteButton.setOnClickListener {
            if (!isSelectionMode) {
                // まだ選択モードでなければ、選択モードに切り替える
                isSelectionMode = true
                adapter.selectionMode = true
                Toast.makeText(this, "削除する項目を選択してください", Toast.LENGTH_SHORT).show()
            } else {
                // 既に選択モードの場合は、選択された項目を削除する
                val selectedAlarms = adapter.getSelectedAlarms()
                if (selectedAlarms.isEmpty()) {
                    Toast.makeText(this, "削除する項目を選択してください", Toast.LENGTH_SHORT).show()
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        selectedAlarms.forEach { alarm ->
                            db.alarmDao().delete(alarm)
                        }
                        // 削除後、最新のリストを取得して RecyclerView を更新
                        val updatedAlarms = db.alarmDao().getAllAlarms()
                        runOnUiThread {
                            adapter.selectionMode = false
                            isSelectionMode = false
                            adapter.clearSelection()
                            adapter.updateAlarms(updatedAlarms)
                            Toast.makeText(this@AlarmActivity, "選択されたアラームを削除しました", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
