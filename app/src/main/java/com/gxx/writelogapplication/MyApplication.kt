package com.gxx.writelogapplication

import android.app.Application
import android.util.Log
import com.gxx.logwritelibrary.LogWrite
import com.gxx.logwritelibrary.inter.OnLogWriteFinishListener
import com.gxx.logwritelibrary.model.TagLogModel
import com.gxx.logwritelibrary.utils.ProcessUtils

class MyApplication: Application(), OnLogWriteFinishListener {
    val TAG = "MyApplication";
    override fun onCreate() {
        super.onCreate()
        LogWrite.Builder()
            .setApplication(this)
            .setDebug(BuildConfig.DEBUG)
            .build()


        if (com.blankj.utilcode.util.ProcessUtils.isMainProcess()){
            LogWrite.setOnLogWriteFinishListener(this)
        }

    }

    override fun onLogWriteFinish(txtPath: String, list: MutableList<TagLogModel>) {
         Log.e(TAG,"本地文件地址---->$txtPath")
        for (tagLogModel in list) {
            Log.e(TAG,tagLogModel.message + "，time=${tagLogModel.time}")
        }
    }
}