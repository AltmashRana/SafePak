package com.example.safepak.logic.models.notification

import com.example.safepak.logic.models.UserLocation

data class NotificationData(
    var userid : String,
    var type : String, //level1, level2, medical, chat, request
    var body : String,
    var title : String,
    var blood : String = ""
)