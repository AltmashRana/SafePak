package com.example.safepak.logic.session

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "safepak"
        private const val TABLE_CALLS = "Call"
        private const val TABLE_RESPONSES = "Response"

        private const val KEY_ID = "id"
        private const val KEY_USERID = "userid"
        private const val KEY_TYPE = "type"
        private const val KEY_TIME = "time"
        private const val KEY_STATUS = "status"

        private const val KEY_CALLID = "callid"
        private const val KEY_STRANGERID = "userid"
    }

    override fun onCreate(db: SQLiteDatabase?) {

        val CREATE_CALLS_TABLE = ("CREATE TABLE " + TABLE_CALLS + " ("
            + KEY_ID + " TEXT PRIMARY KEY," + KEY_USERID + " TEXT," + KEY_TYPE + " TEXT,"
                + KEY_TIME + " TEXT," + KEY_STATUS + " TEXT" +  ")")

        val CREATE_UNKNOWNCALLS_TABLE = ("CREATE TABLE " + TABLE_RESPONSES + " ("
                + KEY_CALLID + " TEXT PRIMARY KEY," + KEY_STRANGERID + " TEXT)")

        db?.execSQL (CREATE_CALLS_TABLE)
        db?.execSQL (CREATE_UNKNOWNCALLS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, old: Int, new: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CALLS")
    }

}