package com.example.safepak.logic.models

data class Message(
    var id: String? = null,
    var senderid: String? = null,
    var receiverid: String? = null,
    var text: String? = null,
    var timestamp: String = "-1",
    var time: String? = null,
    var status: String? = null,
)
