package com.gxx.logwritelibrary.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase

class DBHelper(context: Context, name: String, factory: SQLiteDatabase.CursorFactory?, version: Int) : android.database.sqlite.SQLiteOpenHelper(context, name, factory, version) {
    companion object{
        const val COLUMN_ID = "id"
        const val COLUMN_TAG = "tag"
        const val COLUMN_MESSAGE = "message"
        const val COLUMN_TIME = "time"
        const val COLUMN_CREATE_TIME = "createTime"

        const val TABLE_NAME = "write_log_table"
    }


    override fun onCreate(db: SQLiteDatabase?) {
        val table = "create table ${TABLE_NAME}(" +
                "${COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${COLUMN_TAG} VARCHAR(50)," +
                "${COLUMN_MESSAGE} TEXT," +
                "${COLUMN_TIME} TEXT," +
                "${COLUMN_CREATE_TIME} INTEGER" +
                ");"
        db?.execSQL(table)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }



}