package com.fajar.sub2storyapp.utils

import com.fajar.sub2storyapp.data.UserRepository
import com.fajar.sub2storyapp.data.remote.api.ApiConfig

class Injection {
    companion object {
        fun provideUserRepository(): UserRepository {
            val apiService = ApiConfig.getApiService()
            return UserRepository.getInstance(apiService)
        }
    }
}