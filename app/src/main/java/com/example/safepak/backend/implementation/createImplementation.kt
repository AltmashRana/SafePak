package com.example.safepak.backend.implementation

import com.example.safepak.backend.interfaces.ICreate
import com.example.safepak.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class createImplementation : ICreate {
    private lateinit var auth: FirebaseAuth

    override fun registerUser(user: User) {
        auth = Firebase.auth
    }
}