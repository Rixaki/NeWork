package com.example.nework.auth

import com.example.nework.api.AppApi
import com.example.nework.error.ApiError
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Login @Inject constructor(
    private val appApi: AppApi
) {
    suspend fun login(login: String, pass: String): Result<AuthState> {
        try {
            val response = appApi.signIn(
                login,
                pass
            )

            println("token: ${response.body()?.token ?: "null"}")

            return response.body()?.let { Result.success(it) } ?: Result.failure(
                ApiError(
                    response.code(),
                    response.message()
                )
            )
        } catch (e: Exception) {
            //e.printStackTrace()
            when (e) {
                is IOException -> {
                    return Result.failure(IOException())
                }
                is ApiError -> {
                    return Result.failure(ApiError())
                }
                else -> {
                    return Result.failure(UnknownError())
                }
            }
        }
    }
}