package com.roomedia.babbab.service

import com.roomedia.babbab.BuildConfig
import com.roomedia.babbab.model.DeviceNotificationModel
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface MessageService {
    @Headers("Authorization: key=${BuildConfig.FIREBASE_API_KEY}",
        "Content-Type: application/json")
    @POST("fcm/send")
    suspend fun sendNotification(@Body root: DeviceNotificationModel): ResponseBody
}