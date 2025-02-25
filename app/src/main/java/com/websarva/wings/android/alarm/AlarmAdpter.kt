package com.websarva.wings.android.alarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class AlarmAdapter(
    private var alarms: List<Alarm>,
    private val onEditClick: (Alarm) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    // 選択モードのフラグと、選択されたAlarmのセット
    var selectionMode: Boolean = false
    private val selectedAlarms = mutableSetOf<Alarm>()

    inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnAlarmItem: Button = itemView.findViewById(R.id.btnAlarmItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_button2, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.btnAlarmItem.text = "時刻: ${alarm.time}\n曜日: ${alarm.days}\nアラーム名: ${alarm.alarmName}"
        // 選択状態の場合は背景色を変更
        if (selectionMode && selectedAlarms.contains(alarm)) {
            holder.btnAlarmItem.setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_blue_light))
        } else {
            holder.btnAlarmItem.setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_purple))
        }

        holder.btnAlarmItem.setOnClickListener {
            if (selectionMode) {
                // 選択モードの場合、タップで選択/解除を切り替え
                if (selectedAlarms.contains(alarm)) {
                    selectedAlarms.remove(alarm)
                } else {
                    selectedAlarms.add(alarm)
                }
                notifyItemChanged(position)
            } else {
                // 通常は編集画面へ遷移
                onEditClick(alarm)
            }
        }
    }

    override fun getItemCount(): Int = alarms.size

    fun getSelectedAlarms(): List<Alarm> = selectedAlarms.toList()

    fun clearSelection() {
        selectedAlarms.clear()
        notifyDataSetChanged()
    }

    fun updateAlarms(newAlarms: List<Alarm>) {
        alarms = newAlarms
        notifyDataSetChanged()
    }
}
