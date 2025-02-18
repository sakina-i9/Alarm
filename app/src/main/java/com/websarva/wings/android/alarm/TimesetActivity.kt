package com.websarva.wings.android.alarm

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.*

class TimesetActivity : AppCompatActivity() {

    // クラスレベルで宣言する
    private lateinit var tvTime: TextView
    private lateinit var btnSelectTime: Button

    // ActivityResultLauncher を登録（MIMEタイプ "audio/*" のファイルを取得）
    private val audioPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileName(uri)
            // 選択された音声ファイルのURIが返ってくる
            Toast.makeText(this, "選択された音声ファイル: $uri", Toast.LENGTH_SHORT).show()
            findViewById<TextView>(R.id.Set_Alarm_Music).text = fileName ?: "不明なファイル名"
            // ここで ContentResolver を利用してファイルにアクセスしたり、
            // 独自の処理を行ったりできます。
        }
    }

    // ActivityResultLauncher を登録（MIMEタイプ "audio/*" のファイルを取得）
    private val audioPickerLauncher2 = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileName(uri)
            // 選択された音声ファイルのURIが返ってくる
            Toast.makeText(this, "選択された音声ファイル: $uri", Toast.LENGTH_SHORT).show()
            findViewById<TextView>(R.id.SetAfterMusic).text = fileName ?: "不明なファイル名"
            // ここで ContentResolver を利用してファイルにアクセスしたり、
            // 独自の処理を行ったりできます。
        }
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) {
                fileName = it.getString(nameIndex)
            }
        }
        return fileName
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_timeset)

        // 端末のシステムバー分の余白を設定
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val chipSun = findViewById<Chip>(R.id.chipSun)
        val chipMon = findViewById<Chip>(R.id.chipMon)
        val chipTue = findViewById<Chip>(R.id.chipTue)
        val chipWed = findViewById<Chip>(R.id.chipWed)
        val chipThu = findViewById<Chip>(R.id.chipThu)
        val chipFri = findViewById<Chip>(R.id.chipFri)
        val chipSat = findViewById<Chip>(R.id.chipSat)
        val chipGroupDays = findViewById<ChipGroup>(R.id.chipGroupDays)
        val btnCheckDays = findViewById<Button>(R.id.btnCheckDays)

        // Viewの初期化
        tvTime = findViewById(R.id.tvTime)
        btnSelectTime = findViewById(R.id.btnSelectTime)

        btnCheckDays.setOnClickListener{
            val selectedDays = mutableListOf<String>()

            if (chipSun.isChecked) selectedDays.add("日")
            if (chipMon.isChecked) selectedDays.add("月")
            if (chipTue.isChecked) selectedDays.add("火")
            if (chipWed.isChecked) selectedDays.add("水")
            if (chipThu.isChecked) selectedDays.add("木")
            if (chipFri.isChecked) selectedDays.add("金")
            if (chipSat.isChecked) selectedDays.add("土")

            println("選択された曜日: $selectedDays")
        }

        // ボタンをタップしたときに時刻選択ダイアログを表示
        btnSelectTime.setOnClickListener {
            showTimePickerDialog()
        }
        // レイアウトからボタンを取得
        val btnSelectAudio = findViewById<Button>(R.id.Alarm_Music_Button)
        btnSelectAudio.setOnClickListener {
            // MIMEタイプ "audio/*" を指定してファイルピッカーを起動
            audioPickerLauncher.launch("audio/*")
        }

        val btnSelectAudio2 = findViewById<Button>(R.id.After_Music)
        btnSelectAudio2.setOnClickListener {
            audioPickerLauncher2.launch("audio/*")
        }
    }

    private fun showTimePickerDialog() {
        // 現在の時刻を取得
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // 12時間形式（午前・午後）にするため、is24HourViewをfalseに設定
        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            // 12時間表記に変換して午前/午後を判定
            val amPm = if (selectedHour < 12) "午前" else "午後"
            val hour12 = when {
                selectedHour == 0 -> 12
                selectedHour > 12 -> selectedHour - 12
                else -> selectedHour
            }
            // TextViewにフォーマットして表示
            tvTime.text = String.format("%s %02d:%02d", amPm, hour12, selectedMinute)
        }, currentHour, currentMinute, false)

        timePickerDialog.show()

    }

    private inner class AlaarmNameListener : View.OnClickListener{
        override  fun onClick(view: View){
            val input = findViewById<EditText>(R.id.Alarm_Name)
            val inputstr = input.text.toString()
        }
    }


}
