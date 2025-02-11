package com.websarva.wings.android.alarm

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        val btnSetTime = findViewById<Button>(R.id.btnSetTime)
        val tvSelectedTime = findViewById<TextView>(R.id.tvSelectedTime)

        btnSetTime.setOnClickListener {
            // 現在の時刻を取得
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            // TimePickerDialog を表示
            val timePickerDialog = TimePickerDialog(this,
                { _, selectedHour, selectedMinute ->
                    // 選択した時間をTextViewに表示
                    tvSelectedTime.text = String.format("%02d:%02d", selectedHour, selectedMinute)
                },
                hour, minute, true // 24時間表示
            )

            timePickerDialog.show()
        }
    }
}