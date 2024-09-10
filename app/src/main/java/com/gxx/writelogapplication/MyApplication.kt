package com.gxx.writelogapplication

import android.app.Application
import android.util.Log
import com.gxx.logwritelibrary.LogWriteManager
import com.gxx.logwritelibrary.inter.OnLogWriteTimeFinishListener
import com.gxx.logwritelibrary.model.TagLogModel

class MyApplication: Application(), OnLogWriteTimeFinishListener {
    val TAG = "MyApplication";
    override fun onCreate() {
        super.onCreate()
        LogWriteManager.Builder()
            .setApplication(this)
            .setDebug(BuildConfig.DEBUG)
            .setOnLogWriteTimeFinishListener(this)
            .setDuration(30)
            .build()

    }

    override fun onLogWriteTimeFinish(txtPath: String, list: MutableList<TagLogModel>) {
         Log.e(TAG,"本地文件地址---->$txtPath")
    }
}