package com.gxx.logwritelibrary.utils.pool.inter

interface ITask : Comparable<ITask>{
    fun doTask()
}