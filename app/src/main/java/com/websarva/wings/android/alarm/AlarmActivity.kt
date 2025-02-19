package com.websarva.wings.android.alarm

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
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
import java.util.Calendar

class AlarmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alarm)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Alarm)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val timeset = findViewById<ImageButton>(R.id.add_alarm)
        timeset.setOnClickListener{
            val intent = Intent(this, TimesetActivity::class.java)
        startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAlarms)
        recyclerView.layoutManager = LinearLayoutManager(this)

        CoroutineScope(Dispatchers.IO).launch {
            val db = Room.databaseBuilder(
                applicationContext,
                AlarmDatabase::class.java, "alarms-db"
            ).build()
            val alarms = db.alarmDao().getAllAlarms()

            runOnUiThread {
                // Adapter に渡して RecyclerView にセット
                recyclerView.adapter = AlarmAdapter(alarms) { alarm ->
                    // ボタンタップ時の処理：編集画面へ遷移する例
                    val intent = Intent(this@AlarmActivity, TimesetActivity::class.java)
                    // alarmの情報をExtraで渡すなどして編集画面で初期値として設定可能
                    startActivity(intent)
                }
            }
        }
    }
}
