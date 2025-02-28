package com.websarva.wings.android.alarm

import android.os.Bundle
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

    // 現在表示中の月を管理する Calendar インスタンス
    private val currentCalendar = Calendar.getInstance()

    private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val headerFormat = SimpleDateFormat("yyyy年M月", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // activity_calendar.xml にはヘッダー、曜日見出し、GridView が含まれる前提
        setContentView(R.layout.activity_calendar)

        gridView = findViewById(R.id.gridViewCalendar)
        tvMonthYear = findViewById(R.id.tvMonthYear)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)

        // Room データベースの作成（マイグレーション追加）
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
    }

    private fun updateCalendar() {
        // ヘッダーに月・年を表示
        tvMonthYear.text = headerFormat.format(currentCalendar.time)

        // 当月の日付リストを "yyyy-MM-dd" 形式で生成
        val days = generateDaysForCurrentMonth(currentCalendar)

        CoroutineScope(Dispatchers.IO).launch {
            // DBから当月のイベントを取得し、Map に変換
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

        // セルタップ時に、その日の予定一覧を表示
        gridView.setOnItemClickListener { _, _, position, _ ->
            val selectedDate = generateDaysForCurrentMonth(currentCalendar)[position]
            showScheduleListDialog(selectedDate)
        }
    }

    // 当月の日付リスト（yyyy-MM-dd形式）を生成
    private fun generateDaysForCurrentMonth(calendar: Calendar): List<String> {
        val days = mutableListOf<String>()
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        // 現在の年月の "yyyy-MM" 部分を取得
        val yearMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
        for (day in 1..maxDay) {
            // 日付を "yyyy-MM-dd" 形式にする（例："2025-03-05"）
            days.add("$yearMonth-${String.format("%02d", day)}")
        }
        return days
    }

    // 指定日の予定があればリストダイアログで表示、編集・削除、新規追加が可能
    private fun showScheduleListDialog(date: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val eventsForDay = db.calendarEventDao().getAllEvents().filter { it.date == date }
            runOnUiThread {
                if (eventsForDay.isNotEmpty()) {
                    val scheduleArray = eventsForDay.map { it.schedule }.toTypedArray()
                    AlertDialog.Builder(this@CalendarActivity)
                        .setTitle("${dbDateFormat.parse(date)?.let { SimpleDateFormat("d", Locale.getDefault()).format(it) } ?: date} 日の予定")
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

    // 新規予定追加ダイアログ（複数予定追加用）
    private fun showScheduleInputDialog(date: String) {
        val editText = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("${dbDateFormat.parse(date)?.let { SimpleDateFormat("d", Locale.getDefault()).format(it) } ?: date} 日の新しい予定を追加")
            .setView(editText)
            .setPositiveButton("保存") { _, _ ->
                val schedule = editText.text.toString().trim()
                if (schedule.isNotBlank()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val newEvent = CalendarEvent(date = date, schedule = schedule)
                        db.calendarEventDao().insertEvent(newEvent)
                        runOnUiThread {
                            calendarAdapter.addSchedule(date, schedule)
                            Toast.makeText(this@CalendarActivity, "予定を追加しました", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "予定が入力されていません", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    // 既存予定の編集ダイアログ
    private fun showEditScheduleDialog(event: CalendarEvent) {
        val editText = EditText(this)
        editText.setText(event.schedule)
        AlertDialog.Builder(this)
            .setTitle("予定の編集")
            .setView(editText)
            .setPositiveButton("保存") { _, _ ->
                val newSchedule = editText.text.toString().trim()
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
}
