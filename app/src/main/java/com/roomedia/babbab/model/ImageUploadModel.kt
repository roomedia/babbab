package com.roomedia.babbab.model

import androidx.annotation.Keep

@Keep
data class ImageUploadResponseModel(
    val data: ImageUploadDataModel
)

@Keep
data class ImageUploadDataModel(
    val medium: MediumImageModel
)

@Keep
data class MediumImageModel(
    val url: String
)