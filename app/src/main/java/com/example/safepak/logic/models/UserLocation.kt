package com.example.safepak.logic.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserLocation(
    var longitude : String? = null,
    var latitude : String? = null,
    var address : String? = "unknown",
) : Parcelable
