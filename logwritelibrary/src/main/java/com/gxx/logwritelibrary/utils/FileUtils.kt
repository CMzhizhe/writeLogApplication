package com.gxx.logwritelibrary.utils

import android.content.Context
import com.gxx.logwritelibrary.model.TagLogModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class FileUtils {

   suspend fun writeLineToFile(context: Context,list: MutableList<TagLogModel>):String{
       val path = context.cacheDir.path + File.separator + "log_write.txt"
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