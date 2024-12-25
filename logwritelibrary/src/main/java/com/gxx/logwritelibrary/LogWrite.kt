package com.gxx.logwritelibrary


import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gxx.logwritelibrary.inter.OnLogWriteFinishListener
import com.gxx.logwritelibrary.model.TagLogModel
import com.gxx.logwritelibrary.service.SuspendWindowService
import com.gxx.logwritelibrary.utils.Utils
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Locale


object LogWrite  {
    val TAG = "LogWrite"
    private var isDebug = false;
    private var isStartService = false
    private lateinit var application:Application
    private var onLogWriteFinishListener: OnLogWriteFinishListener? = null;
    private var weakFlowService:WeakReference<SuspendWindowService>? = null

    class Builder{
        var isDebug = false;
        var application:Application? = null;
        var onLogWriteFinishListener: OnLogWriteFinishListener? = null;

        fun setDebug(isDebug:Boolean):Builder{
            this.isDebug = isDebug;
            return this;
        }

        fun setApplication(application:Application):Builder{
            this.application = application;
            return this;
        }


        fun setOnLogWriteFinishListener(onLogWriteFinishListener: OnLogWriteFinishListener):Builder{
            this.onLogWriteFinishListener = onLogWriteFinishListener;
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
        this.onLogWriteFinishListener = builder.onLogWriteFinishListener
    }

    fun log(tag: String,message: String){
        this.log(tag,message,"",true)
    }

    fun log(tag:String, message:String,jsonString:String = "", showLog:Boolean = true){
        if (isDebug && showLog){
            Log.d(tag,message)
        }

        if (isStartService && weakFlowService!=null && weakFlowService?.get()!=null){
            weakFlowService?.get()?.log(tag = tag,msg = message, json = jsonString)
        }
    }

    /**
     * @date 创建时间: 2024/12/20
     * @author gaoxiaoxiong
     * @description 显示视图
     * @param isFastStart 是否快速启动
     */
    fun showView(isFastStart:Boolean = false){
        // 判断是否含有浮窗权限
        Utils.checkSuspendedWindowPermission(application){
            SuspendWindowService.startAndBindService(
                application,
                isDebug = isDebug,
                isFastStart = isFastStart,
                serviceConnection)
        }
    }



    /**
     * @date 创建时间: 2024/12/20
     * @author gaoxiaoxiong
     * @description 关闭视图
     */
    fun hideView(){
        // 判断是否含有浮窗权限
        Utils.checkSuspendedWindowPermission(application){
            application.stopService(Intent(application,SuspendWindowService::class.java))
            application.unbindService(serviceConnection)
        }
    }


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service!=null){
                val binder = service as SuspendWindowService.LocalBinder
                weakFlowService = WeakReference<SuspendWindowService>(binder.getService())
                isStartService = true
            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isStartService = false
            weakFlowService?.clear()
        }

    }

}