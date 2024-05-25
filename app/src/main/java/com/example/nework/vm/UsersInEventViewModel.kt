/*
package com.example.nework.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nework.api.AppApi
import com.example.nework.dto.User
import com.example.nework.model.UserListInEventState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersInEventViewModel @Inject constructor(
    private val appApi: AppApi,
    private val eventId: Int,
) : ViewModel() {
    private val _state = MutableStateFlow(UserListInEventState())
    val state = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        _state.update {
            UserListInEventState(
                loading = true,
            )
        }
        viewModelScope.launch {
            try {
                val response = appApi.getEventById(eventId)
                if (!response.isSuccessful) {
                    throw RuntimeException()
                } else {
                    val body = response.body() ?: throw RuntimeException()
                    _state.update {
                        UserListInEventState(
                            loading = false,
                            error = false,
                            partListIds = body.participantsIds,
                            speakListIds = body.speakerIds
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    UserListInEventState(
                        loading = false,
                        error = true,
                        lastErrorAction = "Load error."
                    )
                }
            }
        }
    }

    private fun loadUsersByIds (
        partIds: List<Int>,
        speakIds: List<Int>
    ) {
        viewModelScope.launch {
            try {
                val partResult = partIds.map {
                    val response = appApi.getUserById(it)
                    response.body() ?: throw RuntimeException()
                }
                val speakResult = speakIds.map {
                    val response = appApi.getUserById(it)
                    response.body() ?: throw RuntimeException()
                }
                _state.update {
                    UserListInEventState(
                        loading = false,
                        error = false,
                        partListIds = partIds,
                        partList = partResult,
                        speakListIds = speakIds,
                        speakList = speakResult,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    UserListInEventState(
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