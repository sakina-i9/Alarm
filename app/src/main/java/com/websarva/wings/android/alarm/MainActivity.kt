package com.websarva.wings.android.alarm

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // カスタムボタンを取得
        val myCustomButton = findViewById<CharacterButton>(R.id.customBtn)

        // クリックイベントを設定
        myCustomButton.setOnClickListener {
            Toast.makeText(this, "カスタムボタンが押されました", Toast.LENGTH_SHORT).show()
        }
    }
}
