package com.roomedia.babbab.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val MESSAGE_URL = "https://fcm.googleapis.com/"
    val messageService: MessageService = Retrofit.Builder()
        .baseUrl(MESSAGE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MessageService::class.java)

    private const val IMAGE_UPLOAD_URL = "https://api.imgbb.com/"
    val imageUploadService: ImageUploadService = Retrofit.Builder()
        .baseUrl(IMAGE_UPLOAD_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ImageUploadService::class.java)
}