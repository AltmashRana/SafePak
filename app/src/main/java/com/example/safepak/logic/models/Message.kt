package com.example.safepak.logic.models

data class Message(
    var id:Int,
    var senderid:Int,
    var receiverid:Int,
    var position: Int,
    var text: String, var time:String,
    var status: Int?
)
