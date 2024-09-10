package com.gxx.writelogapplication.activity


import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.gxx.logwritelibrary.LogWriteManager
import com.gxx.writelogapplication.R

class UserInfoActivity: AppCompatActivity() {
    private val TAG = "UserInfoActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        LogWriteManager.logWrite(TAG,"onCreate")

        this.findViewById<Button>(R.id.bt_click_me).setOnClickListener {
            LogWriteManager.logWrite(TAG,"R.id.bt_click_me ----> 点击了按钮")
        }

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
