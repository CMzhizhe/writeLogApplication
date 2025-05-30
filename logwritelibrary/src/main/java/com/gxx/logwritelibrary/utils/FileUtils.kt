package com.gxx.logwritelibrary.utils

import android.content.Context
import android.util.Log
import com.gxx.logwritelibrary.BuildConfig
import com.gxx.logwritelibrary.LogWrite
import com.gxx.logwritelibrary.model.TagLogModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class FileUtils {
    fun writeLineToFile(context: Context,list: MutableList<TagLogModel>):String{
        val logExternalDir = if (LogWrite.getFilePath().isNullOrEmpty()){
            context.externalCacheDir?.absolutePath + File.separator + "log" + File.separator + "log_write_" + System.currentTimeMillis() + ".txt"
        }else{
            LogWrite.getFilePath()  + File.separator + "log_write_" + System.currentTimeMillis() + ".txt"
        }

       val file = File(logExternalDir)

       if (file.exists()){
           file.delete()
       }

       if (!file.getParentFile().exists()){
           file.getParentFile().mkdirs();
       }

       if (!file.exists()){
           file.createNewFile();
       }

        if(LogWrite.isDebug()) {
          Log.d("FileUtils", "本地缓存的地址---->${file.absolutePath}");
        }

       kotlin.runCatching {
           val fos = FileOutputStream(file,true)
           val outputStreamWriter = OutputStreamWriter(fos, Charsets.UTF_8)
           for (tagLogModel in list) {
               outputStreamWriter.write("${tagLogModel.time}:${tagLogModel.tag}--->${tagLogModel.message} \n")
           }
           outputStreamWriter.close()
           fos.close()
       }

       return file.path
    }
}