package com.gxx.logwritelibrary.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.gxx.logwritelibrary.model.TagLogModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DBWriteUtils(context: Context, dbName: String) {
    private val writableDatabase: SQLiteDatabase
    private var readDatabase: SQLiteDatabase
    init {
        val dbHelper = DBHelper(context, dbName, null, 1)
        writableDatabase = dbHelper.writableDatabase
        readDatabase = dbHelper.readableDatabase
    }


    fun insert(model: TagLogModel){
        writableDatabase.execSQL(
           "insert into ${DBHelper.TABLE_NAME}(" +
                    "${DBHelper.COLUMN_TAG}," +
                    "${DBHelper.COLUMN_MESSAGE}," +
                    "${DBHelper.COLUMN_TIME}," +
                    "${DBHelper.COLUMN_CREATE_TIME}" +
                    ") values(?,?,?,?)",
            arrayOf(
                model.tag,
                model.message,
                model.time,
                model.createTime)
        )
    }


    suspend fun selectAllData():MutableList<TagLogModel> = withContext(Dispatchers.Default){
        val list = mutableListOf<TagLogModel>()
        val cursor = readDatabase.query(DBHelper.TABLE_NAME, arrayOf(
            DBHelper.COLUMN_TAG,
            DBHelper.COLUMN_MESSAGE,
            DBHelper.COLUMN_TIME,
            DBHelper.COLUMN_CREATE_TIME
        ),null,null,null,null,"${DBHelper.COLUMN_CREATE_TIME} ASC")
        while (cursor.moveToNext()){
            val model = TagLogModel(
                tag = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TAG)),
                message = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_MESSAGE)),
                time = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TIME)),
                createTime = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CREATE_TIME))
            )
            list.add(model)
        }
        cursor?.close()
        list
    }

    suspend fun cleanData() = withContext(Dispatchers.Default){
        writableDatabase.delete(DBHelper.TABLE_NAME,null,null)
    }

}