package com.gxx.logwritelibrary.utils.pool

import com.gxx.logwritelibrary.utils.pool.inter.ITask
import java.util.concurrent.BlockingQueue
import java.util.concurrent.PriorityBlockingQueue

class BlockTaskQueue {
    //阻塞队列
    private val mTaskQueue: BlockingQueue<ITask> = PriorityBlockingQueue<ITask>()

    fun addTask(runnable: ITask){
        mTaskQueue.add(runnable)
    }

    @Throws(InterruptedException::class)
    fun take(): ITask? {
        return mTaskQueue.take()
    }

}