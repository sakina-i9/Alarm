package com.websarva.wings.android.alarm

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView

class CharacterButton2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var listener: OnClickListener? = null
    private val tvSelectedTime: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_button2, this, true)
        tvSelectedTime = findViewById(R.id.tvSelectedTime)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        listener = l
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) {
            listener?.let {
                post { it.onClick(this) }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    // 時間を設定する関数を追加
    fun setTimeText(time: String) {
        tvSelectedTime.text = time
    }
}
