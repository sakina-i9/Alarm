package com.websarva.wings.android.alarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class AlarmAdapter(
    private val alarms: List<Alarm>,
    private val onClick: (Alarm) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

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
        // 表示例：時刻、曜日、アラーム名を表示
        holder.btnAlarmItem.text = "時刻: ${alarm.time}\n曜日: ${alarm.days}\nアラーム名: ${alarm.alarmName}"
        holder.btnAlarmItem.setOnClickListener {
            onClick(alarm)
        }
    }

    override fun getItemCount(): Int = alarms.size
}
