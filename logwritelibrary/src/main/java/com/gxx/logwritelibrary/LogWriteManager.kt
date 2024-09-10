package com.gxx.logwritelibrary

import android.app.Application
import android.util.Log
import com.gxx.logwritelibrary.inter.OnLogWriteTimeFinishListener
import com.gxx.logwritelibrary.model.TagLogModel
import com.gxx.logwritelibrary.utils.FileUtils
import com.gxx.logwritelibrary.db.DBWriteUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

object LogWriteManager {
    private var isDebug = false;
    private var application:Application? = null;
    private var duration:Int = 0;//持续时间，单位秒
    private var dbName:String = "";//数据库名称
    private val singleThread = Executors.newSingleThreadExecutor()
    private var onLogWriteTimeFinishListener: OnLogWriteTimeFinishListener? = null;
    private var dbWriteUtils: DBWriteUtils? = null;
    private var isStart = false;
    private var fileUtils = FileUtils()
    private val simpleDataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

    class Builder{
        var isDebug = false;
        var application:Application? = null;
        var duration:Int = 0;//持续时间，单位秒
        var dbName:String = "";//数据库名称
        var onLogWriteTimeFinishListener: OnLogWriteTimeFinishListener? = null;

        fun setDebug(isDebug:Boolean):Builder{
            this.isDebug = isDebug;
            return this;
        }

        fun setApplication(application:Application):Builder{
            this.application = application;
            return this;
        }

        fun setDuration(duration:Int):Builder{
            this.duration = duration;
            return this;
        }

        fun setDbName(dbName:String):Builder{
            this.dbName = dbName;
            return this;
        }

        fun setOnLogWriteTimeFinishListener(onLogWriteTimeFinishListener: OnLogWriteTimeFinishListener):Builder{
            this.onLogWriteTimeFinishListener = onLogWriteTimeFinishListener;
            return this;
        }

        fun build() {
            if(application == null){
                throw Exception("application is null");
            }
            init(this)
        }
    }

    private fun init(builder: Builder){
        this.isDebug = builder.isDebug;
        this.dbName = builder.dbName;
        if(this.dbName.isNullOrEmpty()){
            dbName = "gxx_log_write.db"
        }
        this.application = builder.application;
        this.duration = builder.duration;
        if (this.duration <= 0){
            this.duration = 300
        }
        this.onLogWriteTimeFinishListener = builder.onLogWriteTimeFinishListener

        dbWriteUtils = DBWriteUtils(application!!,dbName)
    }

    /**
      * 是否已经开始，true，开始了
      */
    fun isStart():Boolean{
        return isStart
    }

   suspend fun start(){
        if(isStart){
            return
        }
        isStart = true
        delay(duration * 1000L)
       isStart = false
       withContext(Dispatchers.Default){
           val list = selectAllData()
           val path = fileUtils.writeLineToFile(application!!.baseContext,list)
           cleanData()
           withContext(Dispatchers.Main){
               onLogWriteTimeFinishListener?.onLogWriteTimeFinish(path,list)
           }
       }
    }


    fun logWrite(tag:String,message:String,showLog:Boolean = true){
        if (!isStart || dbWriteUtils == null){
            return
        }
        val longTime = System.currentTimeMillis()
        this.logWrite(TagLogModel(tag,message,longTime,simpleDataFormat.format(longTime)),showLog)
    }


    fun logWrite(model: TagLogModel,showLog:Boolean = true){
        if (!isStart || dbWriteUtils == null){
            return
        }

        if (isDebug && showLog){
            Log.d(model.tag,model.message)
        }

        singleThread.submit {
            dbWriteUtils?.insert(model)
        }
    }


    suspend fun selectAllData():MutableList<TagLogModel>{
       return dbWriteUtils?.selectAllData() ?: mutableListOf()
    }


    suspend fun cleanData(){
        dbWriteUtils?.cleanData()
    }
}