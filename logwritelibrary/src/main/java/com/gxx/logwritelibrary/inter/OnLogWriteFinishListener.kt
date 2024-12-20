package com.gxx.logwritelibrary.inter

import com.gxx.logwritelibrary.model.TagLogModel


interface OnLogWriteFinishListener {
    fun onLogWriteFinish(txtPath:String, list:MutableList<TagLogModel>)
}