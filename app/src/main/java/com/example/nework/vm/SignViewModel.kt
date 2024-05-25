package com.example.nework.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nework.api.AppApi
import com.example.nework.auth.AppAuth
import com.example.nework.auth.AuthState
import com.example.nework.auth.Login
import com.example.nework.auth.Registration
import com.example.nework.dto.MediaUpload
import com.example.nework.error.ApiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val appApi: AppApi
) : ViewModel() {

    private val noAuth = AuthState()
    private val _auth = MutableStateFlow(noAuth)
    val auth: StateFlow<AuthState>
        get() = _auth.asStateFlow()

    private val noResponse: Result<AuthState> = Result.failure<AuthState>(
        ApiError(code = "Initial value")
    )
    private val _response = MutableStateFlow(noResponse)
    val response: StateFlow<Result<AuthState>>
        get() = _response.asStateFlow()

    fun clearResponse() {
        _response.value = noResponse
    }

    fun changeAuth(authState: AuthState) {
        _auth.value = authState
        appAuth.setAuth(
            id = authState.id,
            token = authState.token,
            avatar = authState.avatarUrl
        )
    }

    /*
    fun clearAuth() {
        privateAuth.value = noAuth
    }
     */

    fun login(login: String, pass: String): Unit {
        viewModelScope.launch(SupervisorJob()) {
            _response.value = Login(appApi).login(login, pass)
            val newState = _response.value.getOrNull()
            //println("newstate id ${newState?.id}")
            if (_response.value.isSuccess && newState != null) {
                changeAuth(newState)
            }
        }
    }

    fun register(
        login: String,
        pass: String,
        name: String,
        uploadAvatar: MediaUpload?
    ): Unit {
        viewModelScope.launch() {
            _response.value =
                Registration(appApi).register(login, pass, name, uploadAvatar)
            //retrofit support flow switching for api requests
            //viewModelScope include superjob+dispatcher.main
        }
    }
}
