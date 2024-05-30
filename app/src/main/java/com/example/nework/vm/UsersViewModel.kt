package com.example.nework.vm

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nework.api.AppApi
import com.example.nework.dto.User
import com.example.nework.model.ResponceState
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


//ONLY SHOWING USER LIST
@HiltViewModel
class UsersViewModel @Inject constructor(
    private val appApi: AppApi,
    @Assisted private val userIds: List<Int>,
) : ViewModel() {
    private val _state = MutableStateFlow(ResponceState())
    val state = _state.asStateFlow()

    private val noList = emptyList<User>()
    private val _list = MutableStateFlow(noList)
    val list = _list.asStateFlow()

    init {
        loadUsersByIds(userIds)
    }

    private fun loadUsersByIds (listIds: List<Int>) {
        viewModelScope.launch {
            try {
                val result = listIds.map {
                    val response = appApi.getUserById(it)
                    response.body() ?: throw RuntimeException()
                }
                _state.update { ResponceState() }
                _list.value = result
            } catch (e: Exception) {
                _state.update {
                    ResponceState(
                        loading = false,
                        error = true,
                        lastErrorAction = "Converting from user identifiers error."
                    )
                }
            }
        }
    }
}