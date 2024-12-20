package com.gxx.writelogapplication.activity


import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.gxx.logwritelibrary.LogWrite
import com.gxx.writelogapplication.R

class UserInfoActivity: AppCompatActivity() {
    private val TAG = "UserInfoActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        LogWrite.log(TAG,"onCreate")

        this.findViewById<Button>(R.id.bt_click_me).setOnClickListener {
            LogWrite.log(TAG,"R.id.bt_click_me ----> 点击了按钮")
        }

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
