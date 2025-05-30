# writeLogApplication
自定义记录log到本地，主要用于release环境，无法显示很好调试，显示log的情况下，将日志都记录在文件里面

#### 使用教程
```
  implementation 'com.github.CMzhizhe:writeLogApplication:v1.0.9'

   LogWrite.Builder()
            .setApplication(this)
            .setDebug(BuildConfig.DEBUG)
            .build()


        if (com.blankj.utilcode.util.ProcessUtils.isMainProcess()){
            LogWrite.setOnLogWriteFinishListener(this)
        }
```

需要开启浮窗的权限，如果没有浮窗的权限，无法使用 
```
class MainActivity : AppCompatActivity() {
  private val TAG = "MainActivity";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.findViewById<Button>(R.id.bt_show_view).setOnClickListener {
            LogWrite.showView()
        }

        this.findViewById<Button>(R.id.bt_hide_view).setOnClickListener {
            LogWrite.hideView()
        }

        this.findViewById<Button>(R.id.bt_enter_user).setOnClickListener {
            LogWrite.log(TAG,"R.id.bt_enter_user---->进入用户界面")
            startActivity(Intent(this,UserInfoActivity::class.java))
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        LogWrite.log(TAG,"dispatchTouchEvent---->ev.action---->${ev?.action}")
        return super.dispatchTouchEvent(ev)
    }

    override fun onResume() {
        super.onResume()
        LogWrite.log(TAG,"onResume")
    }

    override fun onPause() {
        super.onPause()
        LogWrite.log(TAG,"onPause")
    }

    override fun onStop() {
        super.onStop()
        LogWrite.log(TAG,"onPause")
    }

}
```
