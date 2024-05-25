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
import com.example.nework.dto.FeedItem
import com.example.nework.dto.MediaUpload
import com.example.nework.model.EventModel
import com.example.nework.model.FeedModelState
import com.example.nework.model.PhotoModel
import com.example.nework.model.PostModel
import com.example.nework.repo.EventRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject

private val empty = Event(
    id = 0,
    authorId = 0,
    author = "",
    authorJob = null,
    authorAvatar = null,
    content = "",
    published = "",
    coords = null,
    link = null,
    likedByMe = false,
    attachment = null,
    datetime = "",
    participatedByMe = false,
)

private val noPhoto = PhotoModel()
private val noList = emptyList<Int>()

@HiltViewModel
@SuppressLint("CheckResult")//suppression warning
class EventViewModel @Inject constructor(
    private val repository: EventRepo,
    private val remoteKeyDao: EventRemoteKeyDao,
    private val appAuth: AppAuth,
): ViewModel() {
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
    val state : LiveData<FeedModelState>
        get() = _state

    private val _coords = MutableLiveData<Coords?>()
    val coords : LiveData<Coords?>
        get() = _coords

    private val _speakers = MutableLiveData<List<Int>>()
    val speakers : LiveData<List<Int>>
        get() = _speakers

    private val _participants = MutableLiveData<List<Int>>()
    val participants : LiveData<List<Int>>
        get() = _participants

    private val edited = MutableLiveData(empty)

    private val _eventCreated = SingleLiveEvent<Unit>()
    val postCreated : LiveData<Unit>
        get() = _eventCreated

    private val _eventCancelled = SingleLiveEvent<Unit>()
    val eventCancelled : LiveData<Unit>
        get() = _eventCancelled

    @Suppress("UNCHECKED_CAST")
    val newerCount: MutableLiveData<Int> = data.flatMapLatest {
        repository.getNewerCount(remoteKeyDao.max()!!)
            .flowOn(Dispatchers.Default)
        //!! getNewerCount catchable with flow(0)
    } as MutableLiveData<Int>
    //mutable for "Fresh posts" GONE after refresh/load

    private val _photo = MutableLiveData<PhotoModel>()
    val photo : LiveData<PhotoModel>
        get() = _photo

    init {
        load()
    }

    private fun clearModels() {
        edited.postValue(empty)
        _photo.postValue(noPhoto)
        _coords.postValue(null)
        _speakers.postValue(noList)
        _participants.postValue(noList)
    }

    private fun load() = viewModelScope.launch {
        try {
            _state.value = FeedModelState(loading = true)
            // repository.stream.cachedIn(viewModelScope).
            _state.value = FeedModelState()
        } catch (e: Exception) {
            _state.value = FeedModelState(error = true)
        }
    }

    fun save() {
        edited.value?.let {
            viewModelScope.launch {
                try {
                    repository.save(
                        event = it,
                        upload = _photo.value?.uri?.let { MediaUpload(it.toFile()) }
                    )
                    _eventCreated.value = Unit
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        clearModels()
    }

    fun changeEventAndSave(content: String) {
        val isEventModelEmpty = (coords.value==null &&
                speakers.value==noList && participants.value==noList)
        val oldPredicated = if (isEventModelEmpty)
            (edited.value?.content == content)
        else
            ((edited.value?.content == content) &&
            (edited.value?.coords == coords.value) &&
            (edited.value?.participantsIds == participants.value) &&
            (edited.value?.speakerIds == speakers.value))

        if (oldPredicated) {
            return
        }
        edited.value = if (isEventModelEmpty)
            edited.value?.copy(content = content)
        else
            edited.value?.copy(
                content = content,
                coords = coords.value,
                participantsIds = participants.value ?: noList,
                speakerIds = speakers.value ?: noList
            )
        edited.value?.let { editedEvent ->
            viewModelScope.launch {
                try {
                    when (_photo.value) {
                        noPhoto -> {
                            //println("no file")
                            repository.save(editedEvent)
                        }

                        else -> _photo.value?.file?.let { file ->
                            //println("name: ${file.name}")
                            repository.save(editedEvent, MediaUpload(file))
                        }
                    }
                    _eventCreated.postValue(Unit)
                } catch (e: Exception) {
                    _state.value = FeedModelState(
                        error = true,
                        lastErrorAction =
                        if (editedEvent.id == 0)
                            "Error with add event."
                        else
                            "Error with edit event."
                    )
                    _eventCancelled.postValue(Unit)
                }
            }
        }
        clearModels()
    }

    fun changePhoto(uri: Uri?) = _photo.postValue(PhotoModel(uri))
    fun changeCoords(coords: Coords? = null) = _coords.postValue(coords)
    fun changeSpeakersList(list: List<Int>) = _speakers.postValue(list)
    fun changeParticipantsList(list: List<Int>) = _participants.postValue(list)

    fun likeById(id: Int) {
        viewModelScope.launch {
            val event = repository.getEventById(id)//antisticking before request answer (only with throw id, not post)
            if (event?.isLikeLoading == false) {
                try {
                    repository.likeById(id)//like and unlike in 1
                } catch (e: Exception) {
                    _state.value = FeedModelState(
                        error = true,
                        lastErrorAction = "Error with like/unlike event."
                    )
                }
            } else {
                _state.value = FeedModelState(
                    error = true,
                    lastErrorAction = "Still no response of like/unlike act."
                )
            }
        }
    }

    fun removeById(id: Int) {
        viewModelScope.launch {
            try {
                _state.value = FeedModelState(loading = true)
                repository.removeById(id)
                _state.value = FeedModelState()
            } catch (e: Exception) {
                _state.value = FeedModelState(
                    error = true,
                    lastErrorAction = "Error with delete event."
                )
            }
        }
    }
}