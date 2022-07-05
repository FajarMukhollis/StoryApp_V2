package com.fajar.sub2storyapp.data.remote.response

import com.google.gson.annotations.SerializedName

class UploadResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)