package com.example.nework.repo

import androidx.lifecycle.LiveData
import com.example.nework.api.AppApi
import com.example.nework.dao.UserDao
import com.example.nework.dto.User
import com.example.nework.dto.UserPreview
import com.example.nework.entity.UserEntity
import com.example.nework.error.ApiError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepoImpl @Inject constructor(
    private val appApi: AppApi,
    private val userDao: UserDao
) : UserRepo {
    override val data: Flow<List<User>> = userDao.getAll().map { list ->
        list.map { ent -> ent.toDto() }
    }

    override suspend fun getAll() {
        val response = appApi.getUsers()

        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }
        val body = response.body() ?: throw ApiError(
            response.code(),
            response.message()
        )
        userDao.insert(body.map { UserEntity.fromDto(it) })
    }

    override suspend fun getUserPreviewById(id: Int): UserPreview {
        val user = userDao.getUserById(id)
        return UserPreview(
            avatar = user.avatar ?: "404",
            name = user.name
        )
    }

    override suspend fun getUserById(id: Int): User {
        return userDao.getUserById(id).toDto()
    }

    override suspend fun clear() {
        userDao.clear()
    }
}