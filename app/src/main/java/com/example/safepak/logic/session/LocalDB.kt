package com.example.safepak.logic.session

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import com.example.safepak.logic.models.Call
import android.database.sqlite.SQLiteDatabase
import com.google.firebase.database.FirebaseDatabase


object LocalDB {

    private val DATABASE_VERSION = 1
    private val DATABASE_NAME = "SafePak"
    private val TABLE_EMERGENCY = "Emergency"

    private const val KEY_ID = "id"
    private const val KEY_USERID = "userid"
    private const val KEY_TYPE = "type"
    private const val KEY_LOCATION = "location"
    private const val KEY_TIME = "time"
    private const val KEY_STATUS = "status"

    fun storeEmergency(context: Context, call: Call): Long {

        val dbhelper = DBHandler(context)

        val db = dbhelper.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(KEY_ID, call.id)
        contentValues.put(KEY_USERID, call.userid)
        contentValues.put(KEY_TYPE, call.type)
        contentValues.put(KEY_LOCATION, call.location)
        contentValues.put(KEY_TIME, call.time)
        contentValues.put(KEY_STATUS, call.status)

        val result = db.insert(TABLE_EMERGENCY, null, contentValues)

        db.close()
        return result
    }

    @SuppressLint("Range")
    fun getEmergency(context: Context): Call? {
        val dbhelper = DBHandler(context)

        val selectQuery = "SELECT * FROM ${TABLE_EMERGENCY}"

        val db = dbhelper.readableDatabase

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            return null
        }

        var id: String = ""
        var user: String = ""
        var type: String
        var location: String
        var time: String
        var status: String

        try {
            var call : Call? = null
            if (cursor.moveToFirst()) {
                id = cursor.getString(cursor.getColumnIndex(KEY_ID))
                user = cursor.getString(cursor.getColumnIndex(KEY_USERID))
                type = cursor.getString(cursor.getColumnIndex(KEY_TYPE))
                location = cursor.getString(cursor.getColumnIndex(KEY_LOCATION))
                time = cursor.getString(cursor.getColumnIndex(KEY_TIME))
                status = cursor.getString(cursor.getColumnIndex(KEY_STATUS))

                call = Call(id, user, type, location, time, status)
                return call
            }
        } catch (e : NullPointerException){
            db.execSQL("DELETE FROM "+ TABLE_EMERGENCY)
            stopCall(user, id)

        }
        db.close()
        return null

    }

    fun deleteEmergency(context: Context, value: String) {
        val dbhelper = DBHandler(context)
        val db = dbhelper.writableDatabase
        db.execSQL("DELETE FROM $TABLE_EMERGENCY WHERE $KEY_ID='$value'")
        db.close()
    }


    private fun stopCall(userid : String, callid : String){
        val query = FirebaseDatabase.getInstance()
            .getReference("/emergency-calls/${userid}/")
        query.child(callid).updateChildren(mapOf("status" to "stopped"))
    }
}