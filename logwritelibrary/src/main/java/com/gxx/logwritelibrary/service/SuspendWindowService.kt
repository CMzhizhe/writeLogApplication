package com.gxx.logwritelibrary.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gxx.logwritelibrary.LogWrite
import com.gxx.logwritelibrary.R
import com.gxx.logwritelibrary.db.DBWriteUtils
import com.gxx.logwritelibrary.model.TagLogModel
import com.gxx.logwritelibrary.utils.FileUtils
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
class SuspendWindowService : LifecycleService(),View.OnClickListener {
    companion object {
        const val FILTER_SERVICE = "com.gxx.logwritelibrary.service.SuspendWindowService"

        const val STATUS_NO = -1//原始状态
        const val STATUS_1 = 1;//正在记录中
        const val STATUS_2 = 2//将数据库里面的数据写入txt文件中
        const val START = "start"
        const val STOP = "stop"

        const val IS_DEBUG = "isDebug"
        const val DB_NAME = "dbName"

        fun startService(context: Context, isDebug: Boolean = false, dbName: String = "") {
            val intent = Intent(context, SuspendWindowService::class.java)
            intent.putExtra(IS_DEBUG, isDebug)
            intent.putExtra(DB_NAME, dbName)
            context.startService(intent)
        }
    }

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
    private var dbName: String = "";//数据库名称
    private var isDebug = false;

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.isDebug = intent?.getBooleanExtra(IS_DEBUG, false) ?: false
        this.dbName = intent?.getStringExtra(DB_NAME) ?: ""
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        if (this.dbName.isNullOrEmpty()) {
            dbName = "gxx_log_write.db"
        }
        dbWriteUtils = DBWriteUtils(application, dbName)
        createView()
        ViewModelMain.tagLogModelLiveData.observe(this) {
            log(it.tag, it.message)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(SuspendWindowService.FILTER_SERVICE))
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

    private fun log(tag: String, message: String) {
        if (dbWriteUtils == null) {
            if (LogWrite.isDebug()){
                Log.d(TAG, "dbWriteUtils 未初始化");
            }
            return
        }

        taskQueue.add(object : ITask {
            override fun doTask() {
                val logTime = System.currentTimeMillis()
                dbWriteUtils?.insert(TagLogModel(tag, message, logTime, simpleDataFormat.format(logTime)))
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