package com.websarva.wings.android.alarm

import android.app.TimePickerDialog
import android.os.Bundle
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

        val myCustomButton = findViewById<CharacterButton2>(R.id.customBtn2)

        myCustomButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this,
                { _, selectedHour, selectedMinute ->
                    val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                    myCustomButton.setTimeText(time)  // カスタムボタンのテキストを変更
                },
                hour, minute, true
            )

            timePickerDialog.show()
        }
    }
}
