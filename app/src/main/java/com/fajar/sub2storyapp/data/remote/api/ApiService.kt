package com.fajar.sub2storyapp.data.remote.api

import com.fajar.sub2storyapp.data.remote.response.LoginResponse
import com.fajar.sub2storyapp.data.remote.response.SignUpResponse
import com.fajar.sub2storyapp.data.remote.response.StoryResponse
import com.fajar.sub2storyapp.data.remote.response.UploadResponse
import com.fajar.sub2storyapp.model.UserLoginModel
import com.fajar.sub2storyapp.model.UserSignUpModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    //User API
    @POST("login")
    suspend fun login(
        @Body user: UserLoginModel
    ): LoginResponse

    @POST("register")
    suspend fun signup(
        @Body user: UserSignUpModel
    ): SignUpResponse

    //Story API
    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): StoryResponse

    @GET("stories")
    suspend fun getStoriesWithLocation(
        @Header("Authorization") token: String,
        @Query("location") location: Int = 1
    ): StoryResponse

    @Multipart
    @POST("/v1/stories")
    suspend fun uploadStory(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") latitude: RequestBody,
        @Part("lon") longitude: RequestBody
    ): UploadResponse
}