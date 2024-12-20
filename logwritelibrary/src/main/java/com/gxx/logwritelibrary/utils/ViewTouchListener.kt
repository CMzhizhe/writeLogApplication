package com.gxx.logwritelibrary.utils

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

class ViewTouchListener(private val wl: WindowManager.LayoutParams, private val windowManager: WindowManager,val clickListener:View.OnClickListener) :
    View.OnTouchListener {
    private var x = 0
    private var y = 0
    private var isClick = false
    private val CLICK_THRESHOLD: Int = 10 // 点击阈值


    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                x = motionEvent.rawX.toInt()
                y = motionEvent.rawY.toInt()
                isClick = true // 假定是点击事件
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val nowX = motionEvent.rawX.toInt()
                val nowY = motionEvent.rawY.toInt()
                val movedX = nowX - x
                val movedY = nowY - y
                val isChange = Math.abs(movedX) > CLICK_THRESHOLD ||  Math.abs(movedY) > CLICK_THRESHOLD
                x = nowX
                y = nowY
                wl.apply {
                    x += movedX
                    y += movedY
                }

                //更新悬浮球控件位置
                if (isChange){
                    isClick = false
                    windowManager.updateViewLayout(view, wl)
                }
                return true
            }

            MotionEvent.ACTION_UP->{
                if (isClick){
                    clickListener.onClick(view)
                }
            }

            else -> {
                return true
            }
        }

        return false
    }
}
