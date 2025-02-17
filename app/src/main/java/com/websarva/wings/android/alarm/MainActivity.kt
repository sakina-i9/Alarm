package com.websarva.wings.android.alarm

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val AlarmButton = findViewById<CharacterButton>(R.id.customBtn_alarm)
        AlarmButton.setchoice(R.drawable.time, getString(R.string.Time))
        AlarmButton.setOnClickListener {
            val intent = Intent(this, AlarmActivity::class.java)
            startActivity(intent)
        }

        val ScaduleButton = findViewById<CharacterButton>(R.id.customBtn_calender)
        ScaduleButton.setchoice(R.drawable.calender,getString(R.string.calender))
        ScaduleButton.setOnClickListener{
            val intent = Intent(this, MemoActivity::class.java)
            startActivity(intent)
        }
    }
}

