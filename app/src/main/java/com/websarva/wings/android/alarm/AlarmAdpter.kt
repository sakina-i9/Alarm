package com.websarva.wings.android.alarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.recyclerview.widget.RecyclerView

class AlarmAdapter(
    private var alarms: List<Alarm>,
    private val onEditClick: (Alarm) -> Unit,
    // Switchの変更時の処理用コールバック
    private val onToggleChange: (Alarm, Boolean) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    // 選択モードのフラグと、選択されたAlarmのセット（削除用など既存の処理）
    var selectionMode: Boolean = false
    private val selectedAlarms = mutableSetOf<Alarm>()

    inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnAlarmItem: Button = itemView.findViewById(R.id.btnAlarmItem)
        val switchEnabled: Switch = itemView.findViewById(R.id.switchEnabled)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_button2, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.btnAlarmItem.text = "時刻: ${alarm.time}\n曜日: ${alarm.days}\nアラーム名: ${alarm.alarmName}"

        // 選択モード中は背景色変更（既存処理）
        if (selectionMode && selectedAlarms.contains(alarm)) {
            holder.btnAlarmItem.setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_blue_light))
        } else {
            holder.btnAlarmItem.setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_purple))
        }

        // Switch の状態をAlarmのenabledで初期化
        holder.switchEnabled.isChecked = alarm.enabled
        // Switch の状態変更リスナー
        holder.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
            // onToggleChange コールバックで、データベース更新やアラームの再設定を行う
            onToggleChange(alarm, isChecked)
        }

        // ボタンのクリック処理
        holder.btnAlarmItem.setOnClickListener {
            if (selectionMode) {
                if (selectedAlarms.contains(alarm)) {
                    selectedAlarms.remove(alarm)
                } else {
                    selectedAlarms.add(alarm)
                }
                notifyItemChanged(position)
            } else {
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
