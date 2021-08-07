package com.roomedia.babbab.service

import com.roomedia.babbab.model.DeviceNotificationModel
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface MessageService {
    @Headers("Authorization: key=AAAAqnGxMBo:APA91bEkovjyqfZIgIeM4OtKIIqrGsuMdzOxhmLergqswO8KqlFS2ZYncx9f7Jf7iLV_8suLuc7JFRHne-CgpuWMp-wpK-_cIjiPqwedGeg9PuUUL1lmYRSy1syyTNAjiR9_OngusIK3",
        "Content-Type: application/json")
    @POST("fcm/send")
    suspend fun sendNotification(@Body root: DeviceNotificationModel): ResponseBody
}