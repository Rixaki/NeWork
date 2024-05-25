package com.example.nework.vm

import androidx.lifecycle.ViewModel
import com.example.nework.auth.AppAuth
import com.example.nework.auth.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appAuth: AppAuth
) : ViewModel() {
    val data: Flow<AuthState> = appAuth
        .authState

    val authenticated: Boolean
        get() = appAuth.authState.value.id != 0
}