package com.roomedia.babbab.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val MESSAGE_URL = "https://fcm.googleapis.com/"
    val messageService = Retrofit.Builder()
        .baseUrl(MESSAGE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MessageService::class.java)
}