package com.example.safepak.backend.interfaces

import com.example.safepak.data.User

interface ICreate {
    fun registerUser(user: User)
}