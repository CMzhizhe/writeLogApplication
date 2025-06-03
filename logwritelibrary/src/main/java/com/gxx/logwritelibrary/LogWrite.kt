package com.gxx.logwritelibrary


import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log
import com.gxx.logwritelibrary.inter.OnLogWriteFinishListener
import com.gxx.logwritelibrary.model.TagLogModel
import com.gxx.logwritelibrary.service.LogService
import com.gxx.logwritelibrary.utils.Utils
import java.text.SimpleDateFormat
import java.util.Locale


object LogWrite  {
    val TAG = "LogWrite"
    private var isDebug = false
    private var isStartService = false
    private lateinit var application:Application
    private var onLogWriteFinishListener: OnLogWriteFinishListener? = null;
    private var serviceMessenger: Messenger? = null
    private var filePath:String? = null
    private var isStartLog = false
    private val simpleDataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

    class Builder{
        var isDebug = false
        var application:Application? = null
        var filePath:String? = null

        fun setFilePath(path:String):Builder{
            this.filePath = path
            return this
        }

        fun setDebug(isDebug:Boolean):Builder{
            this.isDebug = isDebug;
            return this;
        }


        fun setApplication(application:Application):Builder{
            this.application = application;
            return this;
        }

        fun build() {
            if(application == null){
                throw Exception("application is null");
            }
            init(this)
        }
    }

    fun isDebug():Boolean{
        return isDebug
    }

    fun getOnLogWriteFinishListener():OnLogWriteFinishListener?{
        return onLogWriteFinishListener;
    }

    private fun init(builder: Builder){
        this.application = builder.application!!
        this.isDebug = builder.isDebug
        this.filePath = builder.filePath
        LogService.startAndBindService(
            application,
            serviceConnection)
    }


    fun getFilePath():String?{
        return filePath
    }

    fun setStartLog(boolean: Boolean){
        this.isStartLog = boolean
    }

    fun setOnLogWriteFinishListener(onLogWriteFinishListener: OnLogWriteFinishListener){
        this.onLogWriteFinishListener = onLogWriteFinishListener;
    }

    fun log(tag: String,msg: String){
        this.log(tag,msg,"",true)
    }

    fun log(tag:String, msg:String,json:String = "", showLog:Boolean = true){
        if (isDebug && showLog){
            Log.d(tag,msg)
        }

        if (isStartService && isStartLog){
            val logTime = System.currentTimeMillis()
            val model = TagLogModel(
                tag = tag,
                message = msg,
                json = json,
                createTime = logTime,
                time = simpleDataFormat.format(logTime)
            )

            val msg: Message = Message.obtain(null, LogService.MSG_WHAT_1)
            val bundle = Bundle()
            bundle.putParcelable(LogService.PARAMS_TAG_MODEL, model)
            msg.data = bundle
            try {
                serviceMessenger?.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }


    /**
     * 显示视图
     */
    fun showView(){
        if (!isStartService || !isStartLog){
            return
        }

        Utils.checkSuspendedWindowPermission(application){
            if (isStartService){
                val msg: Message = Message.obtain(null, LogService.MSG_WHAT_2)
                serviceMessenger?.send(msg)
            }
        }
    }


    /**
     * 关闭视图
     */
    fun hideView(){
        if (!isStartLog){
            return
        }

        // 判断是否含有浮窗权限
        Utils.checkSuspendedWindowPermission(application){
            application.stopService(Intent(application,LogService::class.java))
            application.unbindService(serviceConnection)
        }
    }


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service!=null){
                serviceMessenger =  Messenger(service);
                isStartService = true
            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isStartService = false
            serviceMessenger = null
        }

    }

}