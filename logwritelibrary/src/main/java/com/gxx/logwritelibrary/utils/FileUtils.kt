package com.gxx.logwritelibrary.utils

import android.content.Context
import android.os.Environment
import com.gxx.logwritelibrary.model.TagLogModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class FileUtils {

   suspend fun writeLineToFile(context: Context,list: MutableList<TagLogModel>):String{
       val logExternalDir = context.externalCacheDir?.absolutePath + File.separator + "log"
       val path = if (logExternalDir.isNullOrEmpty()){
           context.cacheDir.path + File.separator + "log_write_" + System.currentTimeMillis() + ".txt"
       }else{
           logExternalDir + File.separator + "log_write_" + System.currentTimeMillis() + ".txt"
       }

       val file = File(path)

       if (file.exists()){
           file.delete()
       }

       if (!file.getParentFile().exists()){
           file.getParentFile().mkdirs();
       }

       if (!file.exists()){
           file.createNewFile();
       }

       kotlin.runCatching {
           val fos = FileOutputStream(file,true)
           val outputStreamWriter = OutputStreamWriter(fos)
           for (tagLogModel in list) {
               outputStreamWriter.write("${tagLogModel.time}:${tagLogModel.tag}--->${tagLogModel.message} \n")
           }
           outputStreamWriter.close()
           fos.close()
       }

       return file.path
    }
}