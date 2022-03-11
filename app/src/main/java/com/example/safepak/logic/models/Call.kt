package com.example.safepak.logic.models

import com.example.safepak.data.User
import java.util.*

data class Call(
    var id:String? = null,
    var userid:String? = null,
    var type:String? = null,
    var location: UserLocation? = null,
    var time: String? = null,
    var status: String? = null,
    var respondentid: String? = null,
    var responsetime: String? = null,
    var respondentlocation: UserLocation? = null,

)
