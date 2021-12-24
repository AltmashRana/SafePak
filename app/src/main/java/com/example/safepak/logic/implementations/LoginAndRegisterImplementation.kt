package com.example.safepak.logic.implementations

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.safepak.data.User
import com.example.safepak.frontend.home.HomeActivity
import com.example.safepak.frontend.register.VerifyActivity
import com.example.safepak.logic.interfaces.ILoginAndRegister
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginAndRegisterImplementation : ILoginAndRegister {
    private var flag = 0
    override fun registerUser(activity: VerifyActivity, name: String, password: String, phone: String, cnic: String, code: String, codeBySystem: String): Int {
        val db = Firebase.firestore
        val names = name.split(" ")
        val adduser = User(phone, names[0], names[1], password, cnic, phone,null,null,null,null,null)
        while (flag == 0){ }
        return  flag

    }

    fun setFlag(value : Int){
        flag = value
    }

    override fun loginUser(activty: VerifyActivity, phone: String, password: String, code: String, codeBySystem: String): Int {
        TODO("Not yet implemented")
    }

    override fun logout() {
        TODO("Not yet implemented")
    }

}