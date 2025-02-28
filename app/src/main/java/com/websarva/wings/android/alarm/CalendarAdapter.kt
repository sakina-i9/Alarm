package com.websarva.wings.android.alarm

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(
    private val context: Context,
    // 各セルのキーは "yyyy-MM-dd" 形式
    private val dates: List<String>,
    // キー：日付 ("yyyy-MM-dd")、値：その日の予定リスト
    private val scheduleMap: MutableMap<String, MutableList<String>> = mutableMapOf()
) : BaseAdapter() {

    private val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val outputFormat = SimpleDateFormat("d", Locale.getDefault()) // 日だけを表示

    override fun getCount(): Int = dates.size

    override fun getItem(position: Int): Any = dates[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // セル用レイアウトとして calendar_cell.xml を inflate
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.calendar_cell, parent, false)
        val tvDay = view.findViewById<TextView>(R.id.tvDay)
        val tvSchedule = view.findViewById<TextView>(R.id.tvSchedule)
        val dateStr = dates[position]

        // 日付表示は、"yyyy-MM-dd" から日だけに変換
        try {
            val date = inputFormat.parse(dateStr)
            tvDay.text = outputFormat.format(date)
        } catch (e: Exception) {
            tvDay.text = dateStr
        }

        // 予定がある場合は改行区切りで表示し、文字色を赤に
        val schedules = scheduleMap[dateStr]
        if (schedules != null && schedules.isNotEmpty()) {
            tvDay.setTextColor(context.resources.getColor(android.R.color.holo_red_dark, null))
            tvSchedule.text = schedules.joinToString(separator = "\n")
        } else {
            tvDay.setTextColor(context.resources.getColor(android.R.color.black, null))
            tvSchedule.text = ""
        }
        return view
    }

    // 新規予定を追加
    fun addSchedule(date: String, schedule: String) {
        if (scheduleMap.containsKey(date)) {
            scheduleMap[date]?.add(schedule)
        } else {
            scheduleMap[date] = mutableListOf(schedule)
        }
        notifyDataSetChanged()
    }

    // 予定の更新（指定の予定を新しい内容に置き換え）
    fun updateSchedule(date: String, oldSchedule: String, newSchedule: String) {
        scheduleMap[date]?.let { list ->
            val index = list.indexOf(oldSchedule)
            if (index >= 0) {
                list[index] = newSchedule
                notifyDataSetChanged()
            }
        }
    }

    // 予定の削除
    fun removeSchedule(date: String, schedule: String) {
        scheduleMap[date]?.remove(schedule)
        notifyDataSetChanged()
    }
}
