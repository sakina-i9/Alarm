package com.websarva.wings.android.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import com.google.android.material.chip.Chip
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TimesetActivity : AppCompatActivity() {

    private lateinit var tvTime: TextView
    private lateinit var btnSelectTime: Button
    private lateinit var switchEnabled: Switch
    private lateinit var btnSelectBackground: Button

    // Room データベースのインスタンス（lazy 初期化）
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

    // Alarm Music 用ファイル選択
    private val audioPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(it, flags)
            } catch (e: Exception) {
                Toast.makeText(this, "権限取得に失敗しました: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            val fileName = getFileName(it)
            Toast.makeText(this, "選択された音声ファイル: $it", Toast.LENGTH_SHORT).show()
            val tvAlarmMusic = findViewById<TextView>(R.id.Set_Alarm_Music)
            tvAlarmMusic.text = fileName ?: "不明なファイル名"
            tvAlarmMusic.tag = it.toString()
        }
    }

    // After Music 用ファイル選択
    private val audioPickerLauncher2 = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(it, flags)
            } catch (e: Exception) {
                Toast.makeText(this, "権限取得に失敗しました: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            val fileName = getFileName(it)
            Toast.makeText(this, "選択された音声ファイル: $it", Toast.LENGTH_SHORT).show()
            val tvAfterMusic = findViewById<TextView>(R.id.SetAfterMusic)
            tvAfterMusic.text = fileName ?: "不明なファイル名"
            tvAfterMusic.tag = it.toString()
        }
    }

    // uCrop の結果を受け取るための launcher
    private val cropImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let {
                val tvBackground = findViewById<TextView>(R.id.Set_Background)
                // 必要に応じてファイル名も取得
                tvBackground.text = getFileName(it) ?: "画像が選択されました"
                tvBackground.tag = it.toString()
                Log.d("TimesetActivity", "トリミングされた背景画像URI: ${tvBackground.tag}")
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            Log.e("TimesetActivity", "Crop エラー", cropError)
        }
    }

    // 背景画像選択用 launcher：画像選択後、uCrop を起動
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(it, flags)
            } catch (e: Exception) {
                Toast.makeText(this, "背景画像の権限取得に失敗しました: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            // uCrop を起動するための出力先 URI
            val destinationUri = Uri.fromFile(File(cacheDir, "croppedImage.jpg"))
            // 画面サイズの取得
            val metrics = Resources.getSystem().displayMetrics
            val screenWidth = metrics.widthPixels
            val screenHeight = metrics.heightPixels

            // UCrop を画面サイズに合わせたアスペクト比と最大サイズで設定
            val uCrop = UCrop.of(it, destinationUri)
            uCrop.withAspectRatio(screenWidth.toFloat(), screenHeight.toFloat())
            uCrop.withMaxResultSize(screenWidth, screenHeight)
            cropImageLauncher.launch(uCrop.getIntent(this))
        }
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex != -1) {
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

        // 戻るボタンの設定
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // AlarmManager の設定
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }

        // システムバー余白の設定
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ビューの取得
        tvTime = findViewById(R.id.tvTime)
        btnSelectTime = findViewById(R.id.btnSelectTime)
        switchEnabled = findViewById(R.id.switchEnabled)
        val chipSun = findViewById<Chip>(R.id.chipSun)
        val chipMon = findViewById<Chip>(R.id.chipMon)
        val chipTue = findViewById<Chip>(R.id.chipTue)
        val chipWed = findViewById<Chip>(R.id.chipWed)
        val chipThu = findViewById<Chip>(R.id.chipThu)
        val chipFri = findViewById<Chip>(R.id.chipFri)
        val chipSat = findViewById<Chip>(R.id.chipSat)
        val btnSaveAlarm = findViewById<Button>(R.id.btnSaveAlarm)
        val editAlarmName = findViewById<EditText>(R.id.Alarm_Name)
        val tvAlarmMusic = findViewById<TextView>(R.id.Set_Alarm_Music)
        val tvAfterMusic = findViewById<TextView>(R.id.SetAfterMusic)
        val tvBackground = findViewById<TextView>(R.id.Set_Background)
        btnSelectBackground = findViewById(R.id.btnSelectBackground)

        // 背景画像選択ボタン（uCrop を利用してトリミング）
        btnSelectBackground.setOnClickListener {
            imagePickerLauncher.launch(arrayOf("image/*"))
        }

        btnSelectTime.setOnClickListener { showTimePickerDialog() }
        findViewById<Button>(R.id.Alarm_Music_Button).setOnClickListener { audioPickerLauncher.launch(arrayOf("audio/*")) }
        findViewById<Button>(R.id.After_Music).setOnClickListener { audioPickerLauncher2.launch(arrayOf("audio/*")) }

        // 編集モードの場合の初期化
        val mode = intent.getStringExtra("mode")
        if (mode == "edit") {
            tvTime.text = intent.getStringExtra("time") ?: ""
            editAlarmName.setText(intent.getStringExtra("alarmName") ?: "")
            val alarmMusicText = intent.getStringExtra("alarmMusic")
            val afterMusicText = intent.getStringExtra("afterMusic")
            val backgroundText = intent.getStringExtra("backgroundUri")
            Log.d("TimesetActivity", "読み出された背景画像URI: $backgroundText")
            if (!alarmMusicText.isNullOrEmpty() && alarmMusicText.startsWith("content://")) {
                tvAlarmMusic.text = getFileName(Uri.parse(alarmMusicText))
                tvAlarmMusic.tag = alarmMusicText
            } else {
                tvAlarmMusic.text = alarmMusicText ?: ""
                tvAlarmMusic.tag = ""
            }
            if (!afterMusicText.isNullOrEmpty() && afterMusicText.startsWith("content://")) {
                tvAfterMusic.text = getFileName(Uri.parse(afterMusicText))
                tvAfterMusic.tag = afterMusicText
            } else {
                tvAfterMusic.text = afterMusicText ?: ""
                tvAfterMusic.tag = ""
            }
            if (!backgroundText.isNullOrEmpty() && backgroundText.startsWith("content://")) {
                tvBackground.text = getFileName(Uri.parse(backgroundText))
                tvBackground.tag = backgroundText
            } else {
                tvBackground.text = backgroundText ?: "背景未設定"
                tvBackground.tag = ""
            }
            val days = intent.getStringExtra("days")
            if (!days.isNullOrEmpty()) {
                val dayList = days.split(",")
                chipSun.isChecked = dayList.contains("日")
                chipMon.isChecked = dayList.contains("月")
                chipTue.isChecked = dayList.contains("火")
                chipWed.isChecked = dayList.contains("水")
                chipThu.isChecked = dayList.contains("木")
                chipFri.isChecked = dayList.contains("金")
                chipSat.isChecked = dayList.contains("土")
            }
            val enabledState = intent.getBooleanExtra("enabled", true)
            switchEnabled.isChecked = enabledState
        } else {
            switchEnabled.isChecked = true
            tvBackground.text = "背景未設定"
            tvBackground.tag = ""
        }

        btnSaveAlarm.setOnClickListener {
            if (tvTime.text.isNullOrBlank() || tvTime.text.toString() == "00:00") {
                Toast.makeText(this, "有効な時刻を選択してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val timeText = tvTime.text.toString()
            val selectedDays = mutableListOf<String>()
            if (chipSun.isChecked) selectedDays.add("日")
            if (chipMon.isChecked) selectedDays.add("月")
            if (chipTue.isChecked) selectedDays.add("火")
            if (chipWed.isChecked) selectedDays.add("水")
            if (chipThu.isChecked) selectedDays.add("木")
            if (chipFri.isChecked) selectedDays.add("金")
            if (chipSat.isChecked) selectedDays.add("土")

            val alarmName = editAlarmName.text.toString()
            val alarmMusic = tvAlarmMusic.tag?.toString() ?: ""
            val afterMusic = tvAfterMusic.tag?.toString() ?: ""
            val backgroundUri = tvBackground.tag?.toString() ?: ""
            val enabled = switchEnabled.isChecked

            val alarm = if (mode == "edit") {
                val alarmId = intent.getIntExtra("alarmId", -1)
                Alarm(
                    id = alarmId,
                    time = timeText,
                    days = selectedDays.joinToString(","),
                    alarmName = alarmName,
                    alarmMusic = alarmMusic,
                    afterMusic = afterMusic,
                    backgroundUri = backgroundUri,
                    enabled = enabled
                )
            } else {
                Alarm(
                    time = timeText,
                    days = selectedDays.joinToString(","),
                    alarmName = alarmName,
                    alarmMusic = alarmMusic,
                    afterMusic = afterMusic,
                    backgroundUri = backgroundUri,
                    enabled = enabled
                )
            }

            CoroutineScope(Dispatchers.IO).launch {
                if (mode == "edit") {
                    db.alarmDao().update(alarm)
                } else {
                    db.alarmDao().insert(alarm)
                }
                runOnUiThread {
                    Toast.makeText(this@TimesetActivity, "保存完了", Toast.LENGTH_SHORT).show()
                    val targetTimeInMillis = getTargetTimeInMillisFrom(timeText)
                    scheduleAlarm(targetTimeInMillis, alarm)
                    startActivity(Intent(this@TimesetActivity, AlarmActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val amPm = if (selectedHour < 12) "午前" else "午後"
            val hour12 = when {
                selectedHour == 0 -> 12
                selectedHour > 12 -> selectedHour - 12
                else -> selectedHour
            }
            tvTime.text = String.format("%s %02d:%02d", amPm, hour12, selectedMinute)
        }, currentHour, currentMinute, false)
        timePickerDialog.show()
    }

    private fun getTargetTimeInMillisFrom(timeText: String): Long {
        val parts = timeText.split(" ")
        if (parts.size < 2) return System.currentTimeMillis()
        val amPm = parts[0]
        val timeParts = parts[1].split(":")
        if (timeParts.size < 2) return System.currentTimeMillis()
        var hour = timeParts[0].toIntOrNull() ?: 0
        val minute = timeParts[1].toIntOrNull() ?: 0
        if (amPm == "午後" && hour < 12) {
            hour += 12
        } else if (amPm == "午前" && hour == 12) {
            hour = 0
        }
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return calendar.timeInMillis
    }

    private fun scheduleAlarm(timeInMillis: Long, alarm: Alarm) {
        if (!alarm.enabled) {
            Log.d("TimesetActivity", "Alarm disabled, not scheduling")
            return
        }
        if (alarm.days.isNotEmpty()) {
            val today = SimpleDateFormat("E", Locale.JAPAN).format(Calendar.getInstance().time)
            val dayList = alarm.days.split(",")
            if (!dayList.contains(today)) {
                Log.d("TimesetActivity", "Today ($today) is not in alarm.days, not scheduling")
                return
            }
        }
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("alarmId", alarm.id)
            putExtra("alarmMusic", alarm.alarmMusic)
            putExtra("afterMusic", alarm.afterMusic)
            putExtra("backgroundUri", alarm.backgroundUri)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }
}
