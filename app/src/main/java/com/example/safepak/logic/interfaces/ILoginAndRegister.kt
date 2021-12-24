package com.example.safepak.logic.interfaces

import com.example.safepak.frontend.register.VerifyActivity

interface ILoginAndRegister {
    fun registerUser(activity: VerifyActivity, name: String, password: String, phone: String, cnic: String, code: String, codeBySystem: String) :Int
    fun loginUser(activity: VerifyActivity, phone: String, password: String, code: String, codeBySystem: String):Int
    fun logout()
}