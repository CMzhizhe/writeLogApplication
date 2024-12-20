package com.gxx.logwritelibrary.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class DraggableTouchListener implements View.OnTouchListener {
    private float startX;
    private float startY;
    private float lastX;
    private float lastY;
    private boolean isClick;
    private static final int CLICK_THRESHOLD = 10; // 点击阈值

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getRawX();
                startY = event.getRawY();
                lastX = startX;
                lastY = startY;
                isClick = true; // 假定是点击事件
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - lastX;
                float dy = event.getRawY() - lastY;

                // 更新 View 的位置
                float newX = view.getX() + dx;
                float newY = view.getY() + dy;
                view.setX(newX);
                view.setY(newY);

                lastX = event.getRawX();
                lastY = event.getRawY();

                // 如果移动距离超过阈值，不认为是点击
                if (Math.abs(event.getRawX() - startX) > CLICK_THRESHOLD ||
                        Math.abs(event.getRawY() - startY) > CLICK_THRESHOLD) {
                    isClick = false;
                }
                return true;

            case MotionEvent.ACTION_UP:
                // 如果是点击事件，触发点击逻辑
                if (isClick) {
                    if (clickListener != null) {
                        clickListener.onClick(view);
                    }
                }
                return true;

            default:
                return false;
        }
    }

    // 点击事件回调接口
    private View.OnClickListener clickListener;

    public void setOnClickListener(View.OnClickListener listener) {
        this.clickListener = listener;
    }
}

