package com.gxx.writelogapplication.activity


import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.gxx.logwritelibrary.LogWrite
import com.gxx.writelogapplication.R

class UserInfoActivity: AppCompatActivity() {
    private val TAG = "UserInfoActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        LogWrite.log(TAG,"UserInfoActivity---->onCreate")

        this.findViewById<Button>(R.id.bt_click_me).setOnClickListener {
            LogWrite.log(TAG,"UserInfoActivity---->R.id.bt_click_me ----> 点击了按钮")
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        LogWrite.log(TAG,"UserInfoActivity---->dispatchTouchEvent---->ev.action---->${ev?.action}")
        return super.dispatchTouchEvent(ev)
    }

    override fun onResume() {
        super.onResume()
        LogWrite.log(TAG,"UserInfoActivity---->onResume")
    }

    override fun onPause() {
        super.onPause()
        LogWrite.log(TAG,"UserInfoActivity---->onPause")
    }

    override fun onStop() {
        super.onStop()
        LogWrite.log(TAG,"UserInfoActivity---->onStop")
    }
}
