package com.example.nework.auth

import android.content.Context
import com.example.nework.api.AppApi
import com.google.gson.annotations.SerializedName
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val prefs =
        context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _authState: MutableStateFlow<AuthState>

    companion object {
        private const val KEY_ID = "id"
        private const val KEY_TOKEN = "token"
        private const val KEY_AVATAR = "avatarUrl"
    }

    init {
        var id = 0L
        try {
            id = prefs.getLong(KEY_ID, 0L)
        } catch (_: Exception) { //ClassCastException
            val int = prefs.getInt(KEY_ID, 0)
            id = if (int == 0) 0L else int.toLong()
        }
        val token = prefs.getString(KEY_TOKEN, null)
        val avatar = prefs.getString(KEY_AVATAR, null)

        if (id == 0L || token == null) {
            _authState = MutableStateFlow(AuthState())
            with(prefs.edit()) {
                clear()
                apply()
            }
        } else {
            _authState = MutableStateFlow(AuthState(id, token, avatar))
        }
    }

    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String?, avatar: String? = null) {
        _authState.value = AuthState(id, token, avatar)
        with(prefs.edit()) {
            putLong(KEY_ID, id)
            putString(KEY_TOKEN, token)
            putString(KEY_AVATAR, avatar)
            apply()
        }
    }

    //for sign_out
    @Synchronized
    fun removeAuth() {
        _authState.value = AuthState()
        with(prefs.edit()) {
            clear()
            commit()
        }
    }
}

data class AuthState(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("token") val token: String? = null,
    @SerializedName("avatar") val avatarUrl: String? = null,
)