package com.fajar.sub2storyapp.model

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.fajar.sub2storyapp.data.UserRepository
import com.fajar.sub2storyapp.data.local.UserPreference
import com.fajar.sub2storyapp.data.remote.response.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import java.io.File

class UserViewModel(private val userRepo: UserRepository) : ViewModel() {
    private var _loginResponse = MutableLiveData<LoginResponse>()
    val loginResponse: LiveData<LoginResponse> = _loginResponse

    private var _signUpResponse = MutableLiveData<SignUpResponse>()
    val signUpResponse: LiveData<SignUpResponse> = _signUpResponse

    private var _uploadResponse = MutableLiveData<UploadResponse>()
    val uploadResponse: LiveData<UploadResponse> = _uploadResponse

    fun login(user: UserLoginModel, pref: UserPreference) {
        viewModelScope.launch {
            userRepo.authenticate(user, pref).collect { response ->
                _loginResponse.value = response
            }
        }
    }

    fun signUp(user: UserSignUpModel) {
        viewModelScope.launch {
            userRepo.createUser(user).collect { response ->
                _signUpResponse.value = response
            }
        }
    }

    fun logout(pref: UserPreference) {
        viewModelScope.launch {
            pref.clearUser()
        }
    }

    fun loadUser(pref: UserPreference): LiveData<UserDataModel> {
        return pref.getUser().asLiveData()
    }

    fun loadStory(token: String): LiveData<PagingData<ListStoryItem>> {
        return userRepo.getStory(token).cachedIn(viewModelScope)
    }

    fun loadStoryLocation(token: String): LiveData<StoryResponse> {
        return userRepo.getStoryWithLocation(token)
    }

    fun uploadStory(token: String, file: File, description: String, location: LatLng) {
        viewModelScope.launch {
            userRepo.uploadStory(token, file, description, location).collect { response ->
                _uploadResponse.value = response
            }
        }
    }
}