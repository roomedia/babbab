package com.roomedia.babbab.service

import com.roomedia.babbab.model.ImageUploadResponseModel
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ImageUploadService {
    @FormUrlEncoded
    @POST("1/upload?expiration=600&key=c1c90a2c1d62a26b01cfd6531cde3c53")
    suspend fun upload(@Field("image") image: String): ImageUploadResponseModel
}