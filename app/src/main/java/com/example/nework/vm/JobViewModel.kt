package com.example.nework.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nework.api.AppApi
import com.example.nework.dto.Job
import com.example.nework.dto.Post
import com.example.nework.error.ApiError
import com.example.nework.model.ResponceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private val empty = Job(
    id = 0,
    link = "",
    name = "",
    position = "",
    start = "",
    finish = ""
)

//FOR EDIT JOB
@HiltViewModel
class JobViewModel @Inject constructor(
    private val appApi: AppApi,
    private val oldJob: Job? = null
) : ViewModel() {
    private val _job = MutableStateFlow(oldJob ?: empty)
    val job = _job.asStateFlow()

    private val noState = ResponceState()
    private val _state = MutableLiveData(noState)
    val state: LiveData<ResponceState>
        get() = _state

    fun changeJob(newJob: Job) {
        _job.value = newJob
    }

    fun save() {
        _job.value.let { job ->
            viewModelScope.launch {
                try {
                    val response = appApi.saveJob(job)
                    if (!response.isSuccessful) {
                        _state.value = ResponceState(
                            error = true,
                            lastErrorAction = "Saving job error."
                        )
                    }

                    if (response.body() == null) {
                        _state.value = ResponceState(
                            error = true,
                            lastErrorAction = "Responce job is null."
                        )
                    } else {
                        //responce is ok
                        clearModels()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun remove() {
        _job.value.let { job ->
            viewModelScope.launch {
                try {
                    val response = appApi.deleteJob(job)
                    if (!response.isSuccessful) {
                        _state.value = ResponceState(
                            error = true,
                            lastErrorAction = "Deleting job error."
                        )
                    }

                    if (response.body() == null) {
                        _state.value = ResponceState(
                            error = true,
                            lastErrorAction = "Responce job is null."
                        )
                    } else {
                        //responce is ok
                        clearModels()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun clearModels() {
        _job.value = empty
        _state.value = noState
    }
}