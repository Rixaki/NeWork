package com.example.nework.vm

import android.annotation.SuppressLint
import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.nework.auth.AppAuth
import com.example.nework.dao.EventRemoteKeyDao
import com.example.nework.dto.Coords
import com.example.nework.dto.Event
import com.example.nework.dto.EventType
import com.example.nework.dto.FeedItem
import com.example.nework.dto.MediaUpload
import com.example.nework.model.FeedModelState
import com.example.nework.model.PhotoModel
import com.example.nework.repo.EventRepo
import com.example.nework.util.BoardLiveData
import com.example.nework.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

private val empty = Event(
    id = 0,
    authorId = 0,
    author = "",
    authorJob = null,
    authorAvatar = null,
    content = "",
    coords = null,
    videoLink = null,
    likedByMe = false,
    attachment = null,
    participatedByMe = false,
)

private val noPhoto = PhotoModel()
private val noList = emptyList<Long>()
private const val noTime = ""
private val noState = FeedModelState()

@HiltViewModel
@SuppressLint("CheckResult")//suppression warning
class EventViewModel @Inject constructor(
    private val repository: EventRepo,
    private val remoteKeyDao: EventRemoteKeyDao,
    private val appAuth: AppAuth,
) : ViewModel() {
    private val cached = repository
        .data
        .cachedIn(viewModelScope)

    val data: Flow<PagingData<FeedItem>> = appAuth.authState
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { event ->
                    if (event is Event) {
                        event.copy(ownedByMe = event.authorId == myId)
                    } else {
                        event
                    }
                }
            }
        }

    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state

    private val _coords = MutableLiveData<Coords?>()
    val coords: LiveData<Coords?>
        get() = _coords

    private val _speakers = MutableLiveData<List<Long>>()
    val speakers: LiveData<List<Long>>
        get() = _speakers

    private val _eventDate = MutableLiveData<String>()
    private val eventDate: LiveData<String>
        get() = _eventDate

    private val _eventTime = MutableLiveData<String>()
    private val eventTime: LiveData<String>
        get() = _eventTime

    val eventTimeBoardText = BoardLiveData(
        daySource = eventDate,
        timeSource = eventTime
    )

    private val _isOnline = MutableLiveData<Boolean>()
    val isOnline: LiveData<Boolean>
        get() = _isOnline

    val edited = MutableLiveData(empty)

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    private val _eventCancelled = SingleLiveEvent<Unit>()
    val eventCancelled: LiveData<Unit>
        get() = _eventCancelled

    //______________________________________________
    //FOR CARD PAGE
    private val _cardEvent = MutableLiveData(empty)
    val cardEvent: LiveData<Event>
        get() = _cardEvent

    val authenticated: Boolean
        get() = appAuth.authState.value.id != 0L

    fun getPostById(id: Long) {
        _state.postValue(FeedModelState(loading = true))
        val post = repository.getEventById(id)?.copy(ownedByMe = authenticated)
        if (post != null) {
            _state.postValue(noState)
            _cardEvent.postValue(post)
        } else {
            _state.postValue(
                FeedModelState(
                    error = true,
                    lastErrorAction = "No response from post-repository with id $id."
                )
            )
        }
    }
    //______________________________________________

    private val _newerCount = MutableLiveData<Int>()
    val newerCount: LiveData<Int>
        get() = _newerCount

    fun checkNewer() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val max = remoteKeyDao.max() ?: return@launch
                val newerCount = repository.getNewerCount(max).firstOrNull() ?: return@launch
                _newerCount.postValue(newerCount)
            } catch (e: Exception) {
                _newerCount.postValue(0)
            }
        }
    }

    private val _photo = MutableLiveData<PhotoModel>()
    val photo: LiveData<PhotoModel>
        get() = _photo

    init {
        load()
        _isOnline.postValue(edited.value?.type == EventType.ONLINE)
    }

    private fun clearModels() {
        edited.postValue(empty)
        _photo.postValue(noPhoto)
        _coords.postValue(null)
        _speakers.postValue(noList)
        _eventDate.postValue(noTime)
        _eventTime.postValue(noTime)
        _isOnline.postValue(false)
    }

    private fun load() = viewModelScope.launch(Dispatchers.Default) {
        try {
            _state.postValue(FeedModelState(loading = true))
            _state.postValue(FeedModelState())
        } catch (e: Exception) {
            _state.postValue(FeedModelState(error = true))
        }
    }

    fun cancelEdit() {
        edited.postValue(empty)
        clearModels()
        _eventCancelled.postValue(Unit)
    }

    fun save() {
        edited.value?.let {
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    repository.save(
                        event = it,
                        upload = _photo.value?.uri?.let { MediaUpload(it.toFile()) }
                    )
                    _eventCreated.postValue(Unit)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        clearModels()
    }

    fun changeEventAndSave(
        content: String,
        videoLink: String? = null
    ) {
        val isEventModelEmpty = (coords.value == null &&
                speakers.value == noList)
        val oldPredicated = if (isEventModelEmpty)
            ((edited.value?.content == content) &&
                    (edited.value?.videoLink == videoLink))
        else
            ((edited.value?.content == content) &&
                    (edited.value?.videoLink == videoLink) &&
                    (edited.value?.coords == coords.value) &&
                    (edited.value?.speakerIds == speakers.value) &&
                    (edited.value?.type ==
                            (if (isOnline.value == true)
                                EventType.ONLINE else EventType.OFFLINE)) &&
                    //TODO: MAYBE ANOTHER TIME FORMAT, DROP 8 ":00.000Z"
                    //NO CRITICAL FEATURE
                    (edited.value?.datetime?.dropLast(8) == "${eventDate.value}T${eventTime.value}"))

        if (oldPredicated) {
            return
        }
        edited.value = if (isEventModelEmpty)
            edited.value?.copy(content = content)
        else
            edited.value?.copy(
                content = content,
                videoLink = videoLink,
                coords = coords.value,
                speakerIds = speakers.value ?: noList,
                datetime = "${eventDate.value}T${eventTime.value}:00.000Z",
                type = if (isOnline.value == true)
                    EventType.ONLINE else EventType.OFFLINE
            )
        edited.value?.let { editedEvent ->
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    when (_photo.value) {
                        noPhoto -> {
                            repository.save(editedEvent)
                        }

                        else -> _photo.value?.file?.let { file ->
                            repository.save(editedEvent, MediaUpload(file))
                        }
                    }
                    _eventCreated.postValue(Unit)
                } catch (e: Exception) {
                    _state.postValue(
                        FeedModelState(
                            error = true,
                            lastErrorAction =
                            if (editedEvent.id == 0L)
                                "Error with add event."
                            else
                                "Error with edit event."
                        )
                    )
                    _eventCancelled.postValue(Unit)
                }
            }
        }
        clearModels()
    }

    fun changePhoto(uri: Uri?) = _photo.postValue(PhotoModel(uri))
    fun changeCoords(coords: Coords? = null) = _coords.postValue(coords)
    fun changeSpeakersList(list: List<Long>) = _speakers.postValue(list)
    fun changeEventDate(date: String) = _eventDate.postValue(date)
    fun changeEventTime(time: String) = _eventTime.postValue(time)
    fun changeType() {
        val prevStatus = isOnline.value ?: false
        _isOnline.postValue(!prevStatus)
    }

    fun clearCoords() = _coords.postValue(null)
    fun clearPhoto() = _photo.postValue(noPhoto)

    fun likeById(id: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            val event =
                repository.getEventById(id)//antisticking before request answer (only with throw id, not post)
            if (event?.isLikeLoading == false) {
                try {
                    repository.likeById(id)//like and unlike in 1
                } catch (e: Exception) {
                    _state.postValue(
                        FeedModelState(
                            error = true,
                            lastErrorAction = "Error with like/unlike event."
                        )
                    )
                }
            } else {
                _state.postValue(
                    FeedModelState(
                        error = true,
                        lastErrorAction = "Still no response of like/unlike act."
                    )
                )
            }
        }
    }

    fun takePartById(id: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            val event =
                repository.getEventById(id)//antisticking before request answer (only with throw id, not post)
            if (event?.isLikeLoading == false) {
                try {
                    repository.participateById(id)//part and out in 1
                } catch (e: Exception) {
                    _state.postValue(
                        FeedModelState(
                            error = true,
                            lastErrorAction = "Error with taking/cancel to take part event."
                        )
                    )
                }
            } else {
                _state.postValue(
                    FeedModelState(
                        error = true,
                        lastErrorAction = "Still no response of taking/cancel to take part act."
                    )
                )
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                _state.postValue(FeedModelState(loading = true))
                repository.removeById(id)
                _state.postValue(FeedModelState())
            } catch (e: Exception) {
                _state.postValue(
                    FeedModelState(
                        error = true,
                        lastErrorAction = "Error with delete event."
                    )
                )
            }
        }
    }
}