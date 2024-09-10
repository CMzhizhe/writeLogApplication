package com.gxx.logwritelibrary.inter

import com.gxx.logwritelibrary.model.TagLogModel

/**
  * 时间到了通知
  */
interface OnLogWriteTimeFinishListener {
    fun onLogWriteTimeFinish(txtPath:String,list:MutableList<TagLogModel>)
}