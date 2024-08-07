package com.example.nework.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nework.api.AppApi
import com.example.nework.dto.Job
import com.example.nework.model.ResponceState
import com.example.nework.util.SingleLiveEvent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val empty = Job(
    id = 0,
    link = "",
    name = "",
    position = "",
    finish = ""
)

private val noState = ResponceState()
private const val noStr = ""
private val noList = emptyList<Job>()

//FOR EDIT JOB
@HiltViewModel(assistedFactory = JobViewModelFactory::class)
class JobViewModel @AssistedInject constructor(
    private val appApi: AppApi,
    @Assisted private val userId: Long
) : ViewModel() {
    val isActiveView = MutableLiveData<Boolean>(false)

    private val _job = MutableStateFlow(empty)
    val job = _job.asStateFlow()

    private val _state = MutableStateFlow(noState)
    val state = _state.asStateFlow()

    private val _jobList = MutableStateFlow(noList)
    val jobList = _jobList.asStateFlow()

    private val _name = MutableLiveData(noStr)
    val name: LiveData<String>
        get() = _name

    private val _position = MutableLiveData(noStr)
    val position: LiveData<String>
        get() = _position

    private val _start = MutableLiveData(noStr)
    val start: LiveData<String>
        get() = _start

    private val _finish = MutableLiveData(noStr)
    val finish: LiveData<String>
        get() = _finish

    private val _link = MutableLiveData(noStr)
    val link: LiveData<String>
        get() = _link

    private val privateJobCanceled = SingleLiveEvent<Unit>()
    val jobCanceled: LiveData<Unit>
        get() = privateJobCanceled

    private val privateJobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = privateJobCreated

    init {
        loadJobsById(userId)
    }

    private fun loadJobsById(id: Long) {
        _state.update {
            it.copy(loading = true)
        }
        viewModelScope.launch {
            runCatching {
                appApi.getJobsByUser(id)
            }
                .onFailure {
                    _state.update {
                        it.copy(
                            error = true,
                            lastErrorAction = "Get list job error."
                        )
                    }
                }
                .onSuccess { resp ->
                    val list = resp.body() ?: emptyList()
                    if (list.isNotEmpty()) {
                        _jobList.update { list }
                    } else {
                        _state.update {
                            it.copy(
                                error = true,
                                lastErrorAction = "Responce list job error."
                            )
                        }
                    }
                }
        }
    }

    fun changeJob(newJob: Job) {
        _job.value = newJob
    }

    fun changeName(str: String) {
        _name.value = str
    }

    fun changePosition(str: String) {
        _position.value = str
    }

    fun changeStart(str: String) {
        _start.value = str
    }

    fun changeFinish(str: String?) {
        _finish.value = str
    }

    fun changeLink(str: String) {
        _link.value = str
    }

    fun save() {
        val isOld = ((job.value.name == name.value) &&
                (job.value.position == position.value) &&
                (job.value.start == start.value) &&
                (job.value.finish == finish.value) &&
                (job.value.link == link.value))
        if (isOld) {
            return
        } else {
            _state.value = ResponceState(loading = true)
            //NO CRITICAL FEATURE WITH TIME DISPLAY ON BOARDS
            changeJob(
                job.value.copy(
                    name = if (name.value.isNullOrBlank()) job.value.name else name.value!!,
                    position = if (position.value.isNullOrBlank()) job.value.position else position.value!!,
                    start = if (start.value.isNullOrBlank()) job.value.start else start.value!!,
                    finish = if (finish.value.isNullOrBlank()) null else finish.value,
                    link = if (link.value.isNullOrBlank()) null else link.value,
                )
            )
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
                                lastErrorAction = "Response job is null."
                            )
                        } else {
                            //responce is ok
                            clearModels()
                        }
                        privateJobCreated.postValue(Unit)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        privateJobCanceled.postValue(Unit)
                    }
                }
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                val response = appApi.deleteJob(id)
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

    fun cancelEdit() {
        clearModels()
        privateJobCanceled.postValue(Unit)
    }

    fun clearModels() {
        _job.value = empty
        _state.value = noState
        _name.value = noStr
        _position.value = noStr
        _start.value = noStr
        _finish.value = noStr
        _link.value = noStr
    }
}

@AssistedFactory
interface JobViewModelFactory {
    fun create(id: Long): JobViewModel
}