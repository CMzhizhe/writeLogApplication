package com.gxx.logwritelibrary.service

import android.app.Application
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
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
import java.util.concurrent.Executors
import java.util.concurrent.PriorityBlockingQueue


/**
 * @功能:应用外打开Service 有局限性 特殊界面无法显示
 * @User Lmy
 * @Creat 4/15/21 5:28 PM
 * @Compony 永远相信美好的事情即将发生
 */
class LogService : Service(), View.OnClickListener {
    companion object {
        const val PARAMS_TAG_MODEL = "tagLogModel"

        const val MSG_WHAT_1 = 1
        const val MSG_WHAT_2 = 2

        const val STATUS_NO = -1//原始状态
        const val STATUS_1 = 1;//正在记录中
        const val STATUS_2 = 2//将数据库里面的数据写入txt文件中

        fun startAndBindService(
            context: Application,
            serviceConnection: ServiceConnection
        ) {
            val intent = Intent(context, LogService::class.java)
            context.startService(intent)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private val TAG = "${LogWrite.TAG}.Service"
    private var handlerThread: HandlerThread? = null
    private var serviceHandler: ServiceHandler? = null
    private var messenger: Messenger? = null


    private val disCoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var status = STATUS_NO
    private var windowManager: WindowManager? = null
    private var floatRootView: CustomButton? = null//悬浮窗View
    private val taskQueue = PriorityBlockingQueue<ITask>()
    private var fileUtils = FileUtils()
    private var dbWriteUtils: DBWriteUtils? = null;
    private val singleThread = Executors.newSingleThreadExecutor()
    private var dbName: String = "gxx_log_write.db";//数据库名称

    class ServiceHandler(private val service: LogService) : Handler(service.mainLooper) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == MSG_WHAT_1) {
                val bundle = msg.getData();
                bundle.classLoader = TagLogModel::class.java.getClassLoader()
                val model = msg.data.getParcelable<TagLogModel?>(PARAMS_TAG_MODEL)
                if (model != null) {
                    service.addITask(model)
                }
            }else if (msg.what == MSG_WHAT_2){
                service.createView()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return messenger?.binder
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        // 创建后台线程，防止主线程阻塞
        handlerThread = HandlerThread("LogServiceThread");
        handlerThread?.start()
        handlerThread.apply {
            // 使用该线程的 Looper 创建 Handler
            serviceHandler = ServiceHandler(this@LogService);
            // 创建 Messenger 并绑定 Handler
            messenger = Messenger(serviceHandler);
            if (dbWriteUtils == null) {
                dbWriteUtils = DBWriteUtils(application, dbName)
            }
        }
    }

    /**
     * 创建悬浮窗
     */
   private fun createView() {
        if (!LogWrite.isDebug()) {
            return
        }

        if (floatRootView != null) {
            return
        }

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
                width = dip2px(application, 65.0f)
                height = dip2px(application, 65.0f)
                gravity = Gravity.LEFT or Gravity.TOP
                //设置剧中屏幕显示
                x = outMetrics.widthPixels / 2 - width / 2
                y = outMetrics.heightPixels / 2 - height / 2
            }

            // 新建悬浮窗控件
            floatRootView = LayoutInflater.from(this@LogService)
                .inflate(R.layout.view_write_float, null) as CustomButton
            floatRootView?.let { it ->
                val button = it as CustomButton
                button.text = "Start"
                val viewTouchListener = ViewTouchListener(layoutParam, this, this@LogService)
                it.setOnTouchListener(viewTouchListener)
                it.setOnClickListener(this@LogService)
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
            text = "Stop"
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
            text = "Wait"
            disCoroutineScope.launch(Dispatchers.IO) {
                val list = dbWriteUtils?.selectAllData() ?: mutableListOf()
                val path = fileUtils.writeLineToFile(application, list)
                if(LogWrite.isDebug()) {
                  Log.d(TAG, "将数据库的存储写入到本地缓存完成");
                }
                dbWriteUtils?.cleanData()
                withContext(Dispatchers.Main) {
                    status = STATUS_NO
                    text = "Start"
                    LogWrite.getOnLogWriteFinishListener()?.onLogWriteFinish(path, list)
                }
            }
        }
    }

    private fun addITask(tagLogModel: TagLogModel) {
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
        if (status == STATUS_NO) {
            start()
        } else if (status == STATUS_1) {
            stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (LogWrite.isDebug()) {
            Log.d(TAG, "SuspendWindowService 销毁了");
        }
        disCoroutineScope.cancel()
        handlerThread?.quitSafely();
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
    private fun dip2px(context: Context, dpValue: Float): Int {
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