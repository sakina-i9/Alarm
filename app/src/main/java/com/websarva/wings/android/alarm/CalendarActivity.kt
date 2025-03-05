package com.websarva.wings.android.alarm

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var gridView: GridView
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var db: AlarmDatabase
    private lateinit var tvMonthYear: TextView
    private lateinit var btnPrevMonth: TextView
    private lateinit var btnNextMonth: TextView
    private lateinit var btnStopAfterMusic: Button

    // 現在表示中の月を管理する Calendar インスタンス
    private val currentCalendar = Calendar.getInstance()

    // 日付フォーマット
    private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val headerFormat = SimpleDateFormat("yyyy年M月", Locale.getDefault())

    // afterMusic 用 MediaPlayer（CalendarActivity 内で再生）
    private var afterMediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // activity_calendar.xml を利用（後述のレイアウト例参照）
        setContentView(R.layout.activity_calendar)

        gridView = findViewById(R.id.gridViewCalendar)
        tvMonthYear = findViewById(R.id.tvMonthYear)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        btnStopAfterMusic = findViewById(R.id.btnStopAfterMusic)
        // 初期状態は非表示
        btnStopAfterMusic.visibility = Button.GONE

        // Room データベースの作成（マイグレーションも登録）
        db = Room.databaseBuilder(applicationContext, AlarmDatabase::class.java, "alarms-db")
            .addMigrations(AlarmDatabase.MIGRATION_1_2, AlarmDatabase.MIGRATION_2_3)
            .build()

        btnPrevMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }
        btnNextMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        updateCalendar()

        // afterMusic 再生設定：Intent から afterMusic URI を取得
        val afterMusicUriString = intent.getStringExtra("afterMusic")

        Log.d("CalendarActivity", "AfterMusic URI: $afterMusicUriString")

        if (!afterMusicUriString.isNullOrEmpty()) {
            try {
                afterMediaPlayer = MediaPlayer.create(this, android.net.Uri.parse(afterMusicUriString)).apply {
                    isLooping = false
                    start()
                    setOnCompletionListener { mp ->
                        mp.release()
                        afterMediaPlayer = null
                        btnStopAfterMusic.visibility = Button.GONE
                    }
                }
                btnStopAfterMusic.visibility = Button.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(this, "afterMusic の再生に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }

        // 「再生終了」ボタンのリスナー設定
        btnStopAfterMusic.setOnClickListener {
            stopAfterMusic()
        }
    }

    private fun updateCalendar() {
        tvMonthYear.text = headerFormat.format(currentCalendar.time)
        val days = generateDaysForCurrentMonth(currentCalendar)
        CoroutineScope(Dispatchers.IO).launch {
            val events = db.calendarEventDao().getAllEvents().filter { days.contains(it.date) }
            val scheduleMap = mutableMapOf<String, MutableList<String>>()
            events.forEach { event ->
                scheduleMap.getOrPut(event.date) { mutableListOf() }.add(event.schedule)
            }
            calendarAdapter = CalendarAdapter(this@CalendarActivity, days, scheduleMap)
            runOnUiThread {
                gridView.adapter = calendarAdapter
            }
        }
        gridView.setOnItemClickListener { _, _, position, _ ->
            val selectedDate = generateDaysForCurrentMonth(currentCalendar)[position]
            showScheduleListDialog(selectedDate)
        }
    }

    private fun generateDaysForCurrentMonth(calendar: Calendar): List<String> {
        val days = mutableListOf<String>()
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val yearMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
        for (day in 1..maxDay) {
            days.add("$yearMonth-${String.format("%02d", day)}")
        }
        return days
    }

    private fun showScheduleListDialog(date: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val eventsForDay = db.calendarEventDao().getAllEvents().filter { it.date == date }
            runOnUiThread {
                if (eventsForDay.isNotEmpty()) {
                    val scheduleArray = eventsForDay.map { it.schedule }.toTypedArray()
                    AlertDialog.Builder(this@CalendarActivity)
                        .setTitle("${SimpleDateFormat("d", Locale.getDefault()).format(dbDateFormat.parse(date))}日の予定")
                        .setItems(scheduleArray) { _, which ->
                            val selectedEvent = eventsForDay[which]
                            showEditScheduleDialog(selectedEvent)
                        }
                        .setPositiveButton("新規追加") { _, _ ->
                            showScheduleInputDialog(date)
                        }
                        .setNegativeButton("キャンセル", null)
                        .show()
                } else {
                    showScheduleInputDialog(date)
                }
            }
        }
    }

    private fun showScheduleInputDialog(date: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_schedule, null)
        val etSchedule = dialogView.findViewById<EditText>(R.id.etSchedule)

        AlertDialog.Builder(this)
            .setTitle("${SimpleDateFormat("d", Locale.getDefault()).format(dbDateFormat.parse(date))}日の予定を入力")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val schedule = etSchedule.text.toString().trim()
                if (schedule.isNotBlank()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val newEvent = CalendarEvent(date = date, schedule = schedule)
                        db.calendarEventDao().insertEvent(newEvent)
                        runOnUiThread {
                            calendarAdapter.addSchedule(date, schedule)
                            Toast.makeText(this@CalendarActivity, "予定を保存しました", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "予定が入力されていません", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun showEditScheduleDialog(event: CalendarEvent) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_schedule, null)
        val etSchedule = dialogView.findViewById<EditText>(R.id.etSchedule)
        etSchedule.setText(event.schedule)

        AlertDialog.Builder(this)
            .setTitle("予定の編集")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val newSchedule = etSchedule.text.toString().trim()
                CoroutineScope(Dispatchers.IO).launch {
                    if (newSchedule.isNotBlank()) {
                        val updatedEvent = event.copy(schedule = newSchedule)
                        db.calendarEventDao().updateEvent(updatedEvent)
                        runOnUiThread {
                            calendarAdapter.updateSchedule(event.date, event.schedule, newSchedule)
                            Toast.makeText(this@CalendarActivity, "予定を更新しました", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        db.calendarEventDao().deleteEvent(event)
                        runOnUiThread {
                            calendarAdapter.removeSchedule(event.date, event.schedule)
                            Toast.makeText(this@CalendarActivity, "予定を削除しました", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun stopAfterMusic() {
        afterMediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        afterMediaPlayer = null
        btnStopAfterMusic.visibility = Button.GONE
    }
}
