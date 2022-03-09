package com.example.safepak.frontend.blood

interface IBloodBroadcast {
    fun generateBroadcast(blood : String, radius : Double)
    fun stopBroadcast()
    fun changeCircle(radius : Double)
}