# writeLogApplication
自定义记录log到本地，主要用于release环境，无法显示很好调试，显示log的情况下，将日志都记录在文件里面

#### 使用教程
```
  implementation 'com.github.CMzhizhe:writeLogApplication:v1.0.1'

  LogWriteManager.Builder()
            .setApplication(this)
            .setDebug(BuildConfig.DEBUG)
            .setOnLogWriteTimeFinishListener(this)//时间到了，日志记录回调
            .setDuration(30) //单位秒，这里30秒，表示30秒内日志记录倒计时
            .build()
```
在开始使用的时候，需要先调用start()方法开启
```
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.findViewById<Button>(R.id.bt_start).setOnClickListener{
            if(!LogWriteManager.isStart()){
                lifecycleScope.launch {
                    需要先调用start()方法正式开启记录
                    LogWriteManager.start()
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        LogWriteManager.logWrite(TAG,"dispatchTouchEvent---->ev.action---->${ev?.action}")
        return super.dispatchTouchEvent(ev)
    }

    override fun onResume() {
        super.onResume()
        LogWriteManager.logWrite(TAG,"onResume")
    }

    override fun onPause() {
        super.onPause()
        LogWriteManager.logWrite(TAG,"onPause")
    }

    override fun onStop() {
        super.onStop()
        LogWriteManager.logWrite(TAG,"onPause")
    }

}
```
