package com.example.safepak.frontend.services
import com.example.safepak.logic.models.Constants.CONTENT_TYPE
import com.example.safepak.logic.models.Constants.SERVER_KEY
import com.example.safepak.logic.models.notification.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}