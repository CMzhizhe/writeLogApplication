package com.gxx.logwritelibrary.service

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gxx.logwritelibrary.model.TagLogModel

/**
 * @功能: 用于和Service通信
 * @User Lmy
 * @Creat 4/16/21 8:37 AM
 * @Compony 永远相信美好的事情即将发生
 */
object ViewModelMain : ViewModel() {

    val tagLogModelLiveData = MutableLiveData<TagLogModel>()
}