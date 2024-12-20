package com.gxx.logwritelibrary.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.WindowManager

class CustomButton:androidx.appcompat.widget.AppCompatButton {
    private val TAG = "CustomTextView"
    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    )

    private var isClick = false // 是否是点击事件
    private var x = 0
    private var y = 0

    private var wl:WindowManager.LayoutParams? = null
    private var windowManager: WindowManager? = null

    fun setWl(wl:WindowManager.LayoutParams,windowManager: WindowManager){
        this.wl = wl
        this.windowManager = windowManager
        isClickable = true
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                x = event.rawX.toInt()
                y = event.rawY.toInt()
                isClick = true; // 假定是点击事件
            }
            MotionEvent.ACTION_MOVE -> {
                val nowX = event.rawX.toInt()
                val nowY = event.rawY.toInt()
                val movedX = nowX - x
                val movedY = nowY - y
                x = nowX
                y = nowY
                wl.apply {
                    x += movedX
                    y += movedY
                }
                //更新悬浮球控件位置
                windowManager?.updateViewLayout(this, wl)
                isClick = false;
            }
           /* MotionEvent.ACTION_UP->{
                if (isClick) {
                    performClick();
                }
                return true
            }*/
        }
        return super.onTouchEvent(event)
    }

}