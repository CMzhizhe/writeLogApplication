package com.gxx.logwritelibrary


import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gxx.logwritelibrary.inter.OnLogWriteFinishListener
import com.gxx.logwritelibrary.model.TagLogModel
import com.gxx.logwritelibrary.service.SuspendWindowService
import com.gxx.logwritelibrary.service.ViewModelMain
import com.gxx.logwritelibrary.utils.Utils
import java.text.SimpleDateFormat
import java.util.Locale


object LogWrite  {
    val TAG = "LogWrite"
    private var isDebug = false;
    private var dbName = ""
    private var isStartService = false
    private val simpleDataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private lateinit var application:Application
    private var onLogWriteFinishListener: OnLogWriteFinishListener? = null;
    private var isStart = false;

    class Builder{
        var isDebug = false;
        var application:Application? = null;
        var dbName:String = "";//数据库名称
        var onLogWriteFinishListener: OnLogWriteFinishListener? = null;

        fun setDebug(isDebug:Boolean):Builder{
            this.isDebug = isDebug;
            return this;
        }

        fun setApplication(application:Application):Builder{
            this.application = application;
            return this;
        }

        fun setDbName(dbName:String):Builder{
            this.dbName = dbName;
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
        this.isDebug = builder.isDebug;
        this.dbName = builder.dbName
        this.onLogWriteFinishListener = builder.onLogWriteFinishListener
        val filter = IntentFilter(SuspendWindowService.FILTER_SERVICE)
        LocalBroadcastManager.getInstance(application).registerReceiver(receiver,filter)
    }


    fun log(tag:String, message:String, showLog:Boolean = true){
        if (isDebug && showLog){
            Log.d(tag,message)
        }

        //service是否启动了
        if (isStartService){
            //判断是否为主线程
            val logTime = System.currentTimeMillis()
            if (Looper.myLooper() == Looper.getMainLooper()){
                ViewModelMain.tagLogModelLiveData.value = TagLogModel(tag,message,logTime, simpleDataFormat.format(logTime))
            }else{
                ViewModelMain.tagLogModelLiveData.postValue(TagLogModel(tag,message,logTime, simpleDataFormat.format(logTime)))
            }
        }
    }

    /**
     * @date 创建时间: 2024/12/20
     * @author gaoxiaoxiong
     * @description 显示视图
     */
    fun showView(){
        // 判断是否含有浮窗权限
        Utils.checkSuspendedWindowPermission(application){
            SuspendWindowService.startService(application, isDebug = isDebug, dbName = dbName)
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
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            isStartService = true
            if (isDebug()){
                Log.d(TAG,"SuspendWindowService 创建完成")
            }
        }
    }


}