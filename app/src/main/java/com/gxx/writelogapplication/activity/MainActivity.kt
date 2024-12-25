package com.gxx.writelogapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.gxx.logwritelibrary.LogWrite
import com.gxx.writelogapplication.R

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LogWrite.showView(isFastStart = true)

        this.findViewById<Button>(R.id.bt_show_view).setOnClickListener {
            LogWrite.showView()
        }

        this.findViewById<Button>(R.id.bt_hide_view).setOnClickListener {
            LogWrite.hideView()
        }

        this.findViewById<Button>(R.id.bt_enter_user).setOnClickListener {
            LogWrite.log(TAG,"R.id.bt_enter_user---->进入用户界面")
            startActivity(Intent(this,UserInfoActivity::class.java))
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        LogWrite.log(TAG,"dispatchTouchEvent---->ev.action---->${ev?.action}")
        return super.dispatchTouchEvent(ev)
    }

    override fun onResume() {
        super.onResume()
        LogWrite.log(TAG,"onResume")
    }

    override fun onPause() {
        super.onPause()
        LogWrite.log(TAG,"onPause")
    }

    override fun onStop() {
        super.onStop()
        LogWrite.log(TAG,"onPause")
    }

}