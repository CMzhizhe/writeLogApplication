package com.gxx.writelogapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gxx.logwritelibrary.LogWriteManager
import com.gxx.writelogapplication.R
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.findViewById<Button>(R.id.bt_start).setOnClickListener{
            if(!LogWriteManager.isStart()){
                lifecycleScope.launch {
                    LogWriteManager.start()
                }
            }
        }

        this.findViewById<Button>(R.id.bt_enter_user).setOnClickListener {
            LogWriteManager.logWrite(TAG,"R.id.bt_enter_user---->进入用户界面")
            startActivity(Intent(this,UserInfoActivity::class.java))
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        LogWriteManager.logWrite(TAG,"dispatchTouchEvent---->ev.action---->${ev?.action}")
        return super.dispatchTouchEvent(ev)
    }

    override fun onResume() {
        super.onResume()
        LogWriteManager.logWrite(TAG,"onResume")
    }

    override fun onPause() {
        super.onPause()
        LogWriteManager.logWrite(TAG,"onPause")
    }

    override fun onStop() {
        super.onStop()
        LogWriteManager.logWrite(TAG,"onPause")
    }

}