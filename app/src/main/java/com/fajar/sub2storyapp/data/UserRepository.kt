package com.fajar.sub2storyapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.fajar.sub2storyapp.data.local.UserPreference
import com.fajar.sub2storyapp.data.remote.api.ApiService
import com.fajar.sub2storyapp.data.remote.response.*
import com.fajar.sub2storyapp.model.UserDataModel
import com.fajar.sub2storyapp.model.UserLoginModel
import com.fajar.sub2storyapp.model.UserSignUpModel
import com.fajar.sub2storyapp.utils.wrapEspressoIdlingResource
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UserRepository(private val apiService: ApiService) {
    fun setupToken(token: String): String = "Bearer $token"
    fun setupRequestBody(request: String) = request.toRequestBody("text/plain".toMediaType())
    fun fileToMultiPart(file: File): MultipartBody.Part {
        val requestImage = file.asRequestBody("image/jpeg".toMediaType())
        return MultipartBody.Part.createFormData(
            "photo",
            file.name,
            requestImage
        )
    }

    fun authenticate(user: UserLoginModel, pref: UserPreference): Flow<LoginResponse> = flow {
        emit(LoginResponse(null, false, ""))
        wrapEspressoIdlingResource {
            try {
                val response = apiService.login(user)
                val userPreference = UserDataModel(
                    token = response.loginResult?.token as String,
                    isLogin = true
                )
                pref.saveUser(userPreference)
                emit(response)
            } catch (e: Exception) {
                emit(LoginResponse(null, true, e.message.toString()))
            }
        }
    }

    fun createUser(user: UserSignUpModel): Flow<SignUpResponse> = flow {
        emit(SignUpResponse(false, ""))
        wrapEspressoIdlingResource {
            try {
                val response = apiService.signup(user)
                emit(response)
            } catch (e: Exception) {
                emit(SignUpResponse(true, e.message.toString()))
            }
        }
    }

    fun getStory(token: String): LiveData<PagingData<ListStoryItem>> {
        return wrapEspressoIdlingResource {
            Pager(
                config = PagingConfig(pageSize = 10),
                pagingSourceFactory = {
                    StoryPaging(apiService, setupToken(token))
                }
            ).liveData
        }
    }

    fun getStoryWithLocation(token: String): LiveData<StoryResponse> = liveData {
        emit(StoryResponse(emptyList(), false, ""))
        wrapEspressoIdlingResource {
            try {
                val bearerToken = setupToken(token)
                val response = apiService.getStoriesWithLocation(bearerToken)
                emit(response)
            } catch (e: Exception) {
                emit(StoryResponse(emptyList(), true, e.message.toString()))
            }
        }
    }

    fun uploadStory(
        token: String,
        file: File,
        description: String,
        location: LatLng
    ): Flow<UploadResponse> = flow {
        emit(UploadResponse(false, ""))
        wrapEspressoIdlingResource {
            try {
                val bearerToken = setupToken(token)
                val imageMultipart = fileToMultiPart(file)
                val requestBodyDesc = setupRequestBody(description)
                val requestBodyLat = setupRequestBody(location.latitude.toString())
                val requestBodyLon = setupRequestBody(location.longitude.toString())

                val response = apiService.uploadStory(
                    bearerToken,
                    imageMultipart,
                    requestBodyDesc,
                    requestBodyLat,
                    requestBodyLon
                )
                emit(response)
            } catch (e: Exception) {
                emit(UploadResponse(true, e.message.toString()))
            }
        }
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(apiService: ApiService): UserRepository {
            return instance ?: synchronized(this) {
                val userRepo = UserRepository(apiService)
                instance = userRepo
                userRepo
            }
        }
    }
}