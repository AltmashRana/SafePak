package com.example.safepak.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var userid: String? = null,
    var firstname: String? = null,
    var lastname: String? = null,
    var password: String? = null,
    var cnic: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var img: String? = null,
    var gender: String? = null,
    var bloodgroup: String? = null,
    var willingToDonate: Boolean? = null,
    var status: HashMap<String,String> = HashMap(),
    var registrationTokens: MutableList<String> = mutableListOf()
) : Parcelable
