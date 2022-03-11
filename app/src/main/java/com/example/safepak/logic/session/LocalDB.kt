package com.example.safepak.logic.session

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import com.example.safepak.logic.models.Call
import com.google.firebase.database.FirebaseDatabase


object LocalDB {
    private const val TABLE_CALLS = "Call"
    private const val TABLE_RESPONSES = "Response"

    private const val KEY_ID = "id"
    private const val KEY_USERID = "userid"
    private const val KEY_TYPE = "type"
    private const val KEY_TIME = "time"
    private const val KEY_STATUS = "status"

    private const val KEY_CALLID = "callid"
    private const val KEY_STRANGERID = "userid"


    fun storeEmergency(context: Context, call: Call): Long {

        val dbhelper = DBHandler(context)

        val db = dbhelper.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(KEY_ID, call.id)
        contentValues.put(KEY_USERID, call.userid)
        contentValues.put(KEY_TYPE, call.type)
        contentValues.put(KEY_TIME, call.time)
        contentValues.put(KEY_STATUS, call.status)

        val result = db.insert(TABLE_CALLS, null, contentValues)

        db.close()
        return result
    }

    @SuppressLint("Range")
    fun getEmergency(context: Context): Call? {
        val dbhelper = DBHandler(context)

        val selectQuery = "SELECT * FROM $TABLE_CALLS"

        val db = dbhelper.readableDatabase

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            return null
        }

        var id: String = ""
        var user: String = ""
        val type: String
        val time: String
        val status: String

        try {
            var call : Call? = null
            if (cursor.moveToFirst()) {
                id = cursor.getString(cursor.getColumnIndex(KEY_ID))
                user = cursor.getString(cursor.getColumnIndex(KEY_USERID))
                type = cursor.getString(cursor.getColumnIndex(KEY_TYPE))
                time = cursor.getString(cursor.getColumnIndex(KEY_TIME))
                status = cursor.getString(cursor.getColumnIndex(KEY_STATUS))

                call = Call(id, user, type, null, time, status)
                return call
            }
        } catch (e : NullPointerException){
            db.execSQL("DELETE FROM $TABLE_CALLS")
            stopCall(user, id)

        }
        db.close()
        return null

    }

    fun deleteEmergency(context: Context) {
        val dbhelper = DBHandler(context)
        val db = dbhelper.writableDatabase
        db.execSQL("DELETE FROM $TABLE_CALLS")
        db.close()
    }

    fun storeResponses(context: Context, callid: String, userid: String): Long {

        val dbhelper = DBHandler(context)

        val db = dbhelper.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(KEY_CALLID, callid)
        contentValues.put(KEY_STRANGERID, userid)

        val result = db.insert(TABLE_RESPONSES, null, contentValues)

        db.close()
        return result
    }

    @SuppressLint("Range")
    fun getResponses(context: Context): ArrayList<Pair<String, String>>? {
        val dbhelper = DBHandler(context)

        val selectQuery = "SELECT * FROM $TABLE_RESPONSES"

        val db = dbhelper.readableDatabase

        var cursor: Cursor? = null

        val responses = ArrayList<Pair<String, String>>()
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            return null
        }

        var callid: String
        var userid: String

        try {
            if (cursor.moveToFirst()) {
                do {
                    callid = cursor.getString(cursor.getColumnIndex(KEY_ID))
                    userid = cursor.getString(cursor.getColumnIndex(KEY_USERID))

                    responses.add(Pair(callid, userid))

                } while (cursor.moveToNext())

            }
        }catch (e : NullPointerException){
            //db.execSQL("DELETE FROM $TABLE_RESPONSES")
        }
        db.close()

        return responses
    }

    fun deleteResponses(context: Context){
        val dbhelper = DBHandler(context)
        val db = dbhelper.writableDatabase
        db.execSQL("DELETE FROM $TABLE_RESPONSES")
        db.close()
    }

    private fun stopCall(userid : String, callid : String){
        val query = FirebaseDatabase.getInstance()
            .getReference("/emergency-calls/${userid}/")
        query.child(callid).updateChildren(mapOf("status" to "stopped"))
    }
}