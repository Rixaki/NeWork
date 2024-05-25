/*
package com.example.nework.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nework.api.AppApi
import com.example.nework.model.UserListInPostState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersInPostViewModel @Inject constructor(
    private val appApi: AppApi,
    private val postId: Int,
) : ViewModel() {
    private val _state = MutableStateFlow(UserListInPostState())
    val state = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        _state.update {
            it.copy(
                loading = true,
            )
        }
        viewModelScope.launch {
            try {
                val response = appApi.getPostById(postId)
                if (!response.isSuccessful) {
                    throw RuntimeException()
                } else {
                    val body = response.body() ?: throw RuntimeException()
                    _state.update {
                        UserListInPostState(
                            loading = false,
                            error = false,
                            userListIds = body.mentionIds
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    UserListInPostState(
                        loading = false,
                        error = true,
                        lastErrorAction = "Load error."
                    )
                }
            }
        }
    }

    private fun loadUsersByIds (listIds: List<Int>) {
        viewModelScope.launch {
            try {
                val result = listIds.map {
                    val response = appApi.getUserById(it)
                    response.body() ?: throw RuntimeException()
                }
                _state.update {
                    UserListInPostState(
                        loading = false,
                        error = false,
                        userListIds = listIds,
                        userList = result
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    UserListInPostState(
                        loading = false,
                        error = true,
                        lastErrorAction = "Ids converting error."
                    )
                }
            }
        }
    }
}
 */