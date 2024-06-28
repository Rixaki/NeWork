package com.example.nework.repo

import androidx.lifecycle.LiveData
import com.example.nework.dto.User
import com.example.nework.dto.UserPreview
import kotlinx.coroutines.flow.Flow

interface UserRepo {
    val data: Flow<List<User>>

    suspend fun getAll()
    suspend fun getUserPreviewById(id: Long): UserPreview
    suspend fun getUserById(id: Long): User
    suspend fun clear()
}