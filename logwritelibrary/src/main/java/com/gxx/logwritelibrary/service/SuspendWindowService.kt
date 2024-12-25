package com.gxx.logwritelibrary.service

import android.annotation.SuppressLint
import android.app.Application
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gxx.logwritelibrary.BuildConfig
import com.gxx.logwritelibrary.LogWrite
import com.gxx.logwritelibrary.R
import com.gxx.logwritelibrary.db.DBWriteUtils
import com.gxx.logwritelibrary.model.TagLogModel
import com.gxx.logwritelibrary.utils.FileUtils
import com.gxx.logwritelibrary.utils.ProcessUtils
import com.gxx.logwritelibrary.utils.Utils
import com.gxx.logwritelibrary.utils.ViewTouchListener
import com.gxx.logwritelibrary.utils.pool.inter.ITask
import com.gxx.logwritelibrary.widget.CustomButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.PriorityBlockingQueue


/**
 * @功能:应用外打开Service 有局限性 特殊界面无法显示
 * @User Lmy
 * @Creat 4/15/21 5:28 PM
 * @Compony 永远相信美好的事情即将发生
 */
class SuspendWindowService : Service(),View.OnClickListener {
    companion object {
        const val MSG_WHAT_1 = 1

        const val STATUS_NO = -1//原始状态
        const val STATUS_1 = 1;//正在记录中
        const val STATUS_2 = 2//将数据库里面的数据写入txt文件中


        const val IS_DEBUG = "isDebug"
        const val IS_FAST_START = "isFastStart"

        fun startAndBindService(context: Application,
                                isDebug: Boolean = false,
                                isFastStart:Boolean = false,
                                serviceConnection: ServiceConnection) {
            val intent = Intent(context, SuspendWindowService::class.java)
            intent.putExtra(IS_DEBUG, isDebug)
            intent.putExtra(IS_FAST_START,isFastStart)
            context.startService(intent)
            context.bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE)
        }
    }

    private val binder: IBinder = LocalBinder(this)
    private var handler:MyHandler? = null
    private val disCoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var status = STATUS_NO
    private val TAG = "${LogWrite.TAG}.SuspendWinService"
    private var windowManager: WindowManager? = null
    private var floatRootView: CustomButton? = null//悬浮窗View
    private val taskQueue = PriorityBlockingQueue<ITask>()
    private var fileUtils = FileUtils()
    private val simpleDataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private var dbWriteUtils: DBWriteUtils? = null;
    private val singleThread = Executors.newSingleThreadExecutor()
    private var dbName: String = "gxx_log_write.db";//数据库名称
    private var isDebug = false;
    private var isFastStart = false//是否快速启动

    class LocalBinder(private val service: SuspendWindowService) : Binder() {
        fun getService():SuspendWindowService{
            return service
        }
    }

    class MyHandler(private val service:SuspendWindowService) : Handler(service.mainLooper){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == MSG_WHAT_1){
                service.addITask(msg.obj as TagLogModel)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.isDebug = intent?.getBooleanExtra(IS_DEBUG, false) ?: false
        this.isFastStart = intent?.getBooleanExtra(IS_FAST_START,false) ?: false

        if (dbWriteUtils == null){
            //获取进程名称
            val processName = ProcessUtils.getProcessName(this)
            // 进行拆分
            val array = processName.split(":")

            if (array.size == 2){
                dbName = array[1] +"_"+dbName
            }

            dbWriteUtils = DBWriteUtils(application, dbName)
        }

        if (isFastStart){
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "开启快速启动");
            }
            start()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        createView()
        handler = MyHandler(this)
    }

    /**
     * 创建悬浮窗
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun createView() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager?.apply {
            val outMetrics = DisplayMetrics()
            defaultDisplay.getMetrics(outMetrics)
            val layoutParam = WindowManager.LayoutParams().apply {
                /**
                 * 设置type 这里进行了兼容
                 */
                type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                format = PixelFormat.RGBA_8888
                flags =
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                //位置大小设置
                width = dip2px(application,65.0f)
                height = dip2px(application,65.0f)
                gravity = Gravity.LEFT or Gravity.TOP
                //设置剧中屏幕显示
                x = outMetrics.widthPixels / 2 - width / 2
                y = outMetrics.heightPixels / 2 - height / 2
            }

            // 新建悬浮窗控件
            floatRootView = LayoutInflater.from(this@SuspendWindowService)
                .inflate(R.layout.view_write_float, null) as CustomButton
            floatRootView?.let { it ->
                val button = it as CustomButton
                button.text = "Start"
                val viewTouchListener = ViewTouchListener(layoutParam, this,this@SuspendWindowService)
                it.setOnTouchListener(viewTouchListener)
                it.setOnClickListener(this@SuspendWindowService)
                // 将悬浮窗控件添加到WindowManager
                this.addView(it, layoutParam)
            }
        }
    }

    private fun start() {
        if (status != STATUS_NO) {
            if (LogWrite.isDebug()) {
                Log.d(TAG, "当前状态=${status}，不满足条件，无法执行");
            }
            return
        }

        if(BuildConfig.DEBUG) {
          Log.d(TAG, "floatRootView == null");
        }

        floatRootView?.apply {
            status = STATUS_1
            val button = this as CustomButton
            button.text = "Stop"
            singleThread.submit {
                while (status == STATUS_1) {
                    kotlin.runCatching {
                        val iTask = taskQueue.take()
                        iTask?.doTask()
                    }.onFailure {
                        it.printStackTrace()
                    }
                }
            }
        }
    }

    private fun stop() {
        if (status != STATUS_1) {
            if (LogWrite.isDebug()) {
                Log.d(TAG, "当前状态=${status}，不满足条件，无法执行");
            }
            return
        }

        floatRootView?.apply {
            status = STATUS_2
            val button = this as CustomButton
            button.text = "Waiting"
            disCoroutineScope.launch(Dispatchers.IO) {
                val list = dbWriteUtils?.selectAllData() ?: mutableListOf()
                val path = fileUtils.writeLineToFile(application, list)
                dbWriteUtils?.cleanData()
                withContext(Dispatchers.Main) {
                    status = STATUS_NO
                    button.text = "Start"
                    LogWrite.getOnLogWriteFinishListener()?.onLogWriteFinish(path,list)
                }
            }
        }
    }



    fun log(tag: String, msg: String,json:String) {
        if (dbWriteUtils == null) {
            if (LogWrite.isDebug()){
                Log.d(TAG, "dbWriteUtils 未初始化");
            }
            return
        }

        val logTime = System.currentTimeMillis()
        if (Looper.myLooper() == Looper.getMainLooper()){
            val model = TagLogModel(
                tag = tag,
                message = msg,
                json = json,
                createTime = logTime,
                time =  simpleDataFormat.format(logTime))
            addITask(model)
        }else{
            val model = TagLogModel(
                tag = tag,
                message = msg,
                json = json,
                createTime = logTime,
                time =  simpleDataFormat.format(logTime))

            val message = Message()
            message.what = MSG_WHAT_1
            message.obj = model
            handler?.sendMessage(message)
        }
    }


    private fun addITask(tagLogModel:TagLogModel){
        taskQueue.add(object : ITask {
            override fun doTask() {
                dbWriteUtils?.insert(tagLogModel)
            }

            override fun compareTo(other: ITask): Int {
                return 1
            }
        })
    }


    override fun onClick(v: View?) {
        if (status == STATUS_NO){
            start()
        }else if (status == STATUS_1){
            stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(LogWrite.isDebug()) {
          Log.d(TAG, "SuspendWindowService 销毁了");
        }
        disCoroutineScope.cancel()
        if (!Utils.isNull(floatRootView)) {
            if (!Utils.isNull(floatRootView?.windowToken)) {
                if (!Utils.isNull(windowManager)) {
                    windowManager?.removeView(floatRootView)
                }
            }
        }
    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
  private  fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    private fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }


}