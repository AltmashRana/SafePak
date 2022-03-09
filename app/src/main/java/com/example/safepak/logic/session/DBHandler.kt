package com.example.safepak.logic.session

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

class DBHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "safepak"
        private const val TABLE_DONOR = "Emergency"

        private const val KEY_ID = "id"
        private const val KEY_USERID = "userid"
        private const val KEY_TYPE = "type"
        private const val KEY_LOCATION = "location"
        private const val KEY_TIME = "time"
        private const val KEY_STATUS = "status"
    }

    override fun onCreate(db: SQLiteDatabase?) {

        val CREATE_DONORS_TABLE = ("CREATE TABLE " + TABLE_DONOR + " ("
            + KEY_ID + " TEXT PRIMARY KEY," + KEY_USERID + " TEXT," + KEY_TYPE + " TEXT," + KEY_LOCATION +
                " TEXT," + KEY_TIME + " TEXT," + KEY_STATUS + " TEXT" +  ")")

        db?.execSQL (CREATE_DONORS_TABLE)
        }

    override fun onUpgrade(db: SQLiteDatabase?, old: Int, new: Int) {
        db?.execSQL("DROP TABLE IF EXISTS" + TABLE_DONOR)
    }

}