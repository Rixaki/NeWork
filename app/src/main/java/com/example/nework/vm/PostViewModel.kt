package com.example.nework.vm

import android.annotation.SuppressLint
import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.example.nework.auth.AppAuth
import com.example.nework.dao.PostRemoteKeyDao
import com.example.nework.dto.Coords
import com.example.nework.dto.FeedItem
import com.example.nework.dto.MediaUpload
import com.example.nework.dto.Post
import com.example.nework.model.FeedModelState
import com.example.nework.model.PhotoModel
import com.example.nework.model.ResponceState
import com.example.nework.repo.PostRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
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
    mentionedMe = false
)

private val noPhoto = PhotoModel()
private val noList = emptyList<Long>()
private val noState = FeedModelState()

@HiltViewModel(assistedFactory = PostViewModelFactory::class)
@SuppressLint("CheckResult")//suppression warning
class PostViewModel @AssistedInject constructor(
    private val repository: PostRepo,
    private val remoteKeyDao: PostRemoteKeyDao,
    private val appAuth: AppAuth,
    @Assisted private val isWallOption: Boolean = false
) : ViewModel() {
    init {
        repository.isWall = isWallOption
    }

    private val cached = repository
        .data
        .cachedIn(viewModelScope)

    val data: Flow<PagingData<FeedItem>> = appAuth.authState
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { post ->
                    if (post is Post) {
                        post.copy(ownedByMe = post.authorId == myId)
                    } else {
                        post
                    }
                }
            }
        }

    private val _state = MutableLiveData<FeedModelState>(noState)
    val state: LiveData<FeedModelState>
        get() = _state

    private val _coords = MutableLiveData<Coords?>(null)
    val coords: LiveData<Coords?>
        get() = _coords

    private val _list = MutableLiveData<List<Long>>(noList)
    val list: LiveData<List<Long>>
        get() = _list

    val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _postCancelled = SingleLiveEvent<Unit>()
    val postCancelled: LiveData<Unit>
        get() = _postCancelled

    //______________________________________________
    //FOR CARD PAGE
    private val _cardPost = MutableLiveData(empty)
    val cardPost: LiveData<Post>
        get() = _cardPost

    val authenticated: Boolean
        get() = appAuth.authState.value.id != 0L

    fun getPostById(id: Long) {
        _state.postValue(FeedModelState(loading = true))
        val post = repository.getPostById(id)?.copy(ownedByMe = authenticated)
        if (post != null) {
            _state.postValue(noState)
            _cardPost.postValue(post)
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

    private val _newerCount = MutableLiveData<Int>(0)
    val newerCount: LiveData<Int>
        get() = _newerCount

    fun checkNewer() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val max = remoteKeyDao.max() ?: return@launch
                _newerCount.postValue(repository.getNewerCount(max)
                    .asLiveData(Dispatchers.Default).value)
            } catch (e: Exception) {
                _newerCount.postValue(0)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    /*
    fun newerCount(): LiveData<Int> = viewModelScope.launch {
        if (!isWallOption) {
            repository.getNewerCount(remoteKeyDao.max()!!)
        } else {
            flowOf(0)
        }
            .asLiveData(Dispatchers.Default)
        }
        //!! getNewerCount catchable with flow(0)
    //mutable for "Fresh posts" GONE after refresh/load
     */

    private val _photo = MutableLiveData<PhotoModel>()
    val photo: LiveData<PhotoModel>
        get() = _photo

    init {
        load()
    }

    fun clearModels() {
        edited.postValue(empty)
        _photo.postValue(noPhoto)
        _coords.postValue(null)
        _list.postValue(noList)
    }

    private fun load() = viewModelScope.launch(Dispatchers.Default) {
        try {
            _state.postValue(FeedModelState(loading = true))
            //repository.stream.cachedIn(viewModelScope).
            _state.postValue(FeedModelState())
        } catch (e: Exception) {
            _state.postValue(FeedModelState(error = true))
        }
    }

    /*
    fun edit(post: Post) {
        edited.value = post
    }
     */

    fun cancelEdit() {
        edited.postValue(empty)
        _postCancelled.postValue(Unit)
    }

    fun save() {
        edited.value?.let {
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    repository.save(
                        post = it,
                        upload = _photo.value?.uri?.let { MediaUpload(it.toFile()) }
                    )
                    _postCreated.postValue (Unit)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        clearModels()
    }

    fun changePostAndSave(content: String) {
        val isPostModelEmpty = (coords.value == null && list.value == noList)
        val oldPredicated = if (isPostModelEmpty)
            (edited.value?.content == content)
        else
            ((edited.value?.content == content) &&
                    (edited.value?.coords == coords.value) &&
                    (edited.value?.mentionIds == list.value))

        if (oldPredicated) {
            return
        }
        edited.postValue ( if (isPostModelEmpty)
            edited.value?.copy(content = content)
        else
            edited.value?.copy(
                content = content,
                coords = coords.value,
                mentionIds = list.value ?: emptyList()
            )
        )
        edited.value?.let { editedPost ->
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    when (_photo.value) {
                        noPhoto -> {
                            //println("no file")
                            repository.save(editedPost)
                        }

                        else -> _photo.value?.file?.let { file ->
                            //println("name: ${file.name}")
                            repository.save(editedPost, MediaUpload(file))
                        }
                    }
                    _postCreated.postValue(Unit)
                } catch (e: Exception) {
                    _state.postValue (FeedModelState(
                        error = true,
                        lastErrorAction =
                        if (editedPost.id == 0L)
                            "Error with add post."
                        else
                            "Error with edit post."
                    ))
                    _postCancelled.postValue(Unit)
                }
            }
        }
        clearModels()
    }

    fun changePhoto(uri: Uri?) = _photo.postValue(PhotoModel(uri))
    fun changeCoords(coords: Coords? = null) = _coords.postValue(coords)
    fun changeMentionList(list: List<Long>) = _list.postValue(list)
    fun clearCoords() = _coords.postValue(null)
    fun clearPhoto() = _photo.postValue(noPhoto)
    fun clearMentionList() = _list.postValue(noList)

    fun likeById(id: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            val post =
                repository.getPostById(id)//antisticking before request answer (only with throw id, not post)
            if (post?.isLikeLoading == false) {
                try {
                    repository.likeById(id)//like and unlike in 1
                } catch (e: Exception) {
                    _state.postValue ( FeedModelState(
                        error = true,
                        lastErrorAction = "Error with like/unlike post."
                    ))
                }
            } else {
                _state.postValue ( FeedModelState(
                    error = true,
                    lastErrorAction = "Still no response of like/unlike act."
                ))
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                _state.postValue ( FeedModelState(loading = true))
                repository.removeById(id)
                _state.postValue ( FeedModelState())
            } catch (e: Exception) {
                _state.postValue ( FeedModelState(
                    error = true,
                    lastErrorAction = "Error with delete post."
                ))
            }
        }
    }


    /*
    //refresh is NOT necessary due to paging
    fun refresh() = viewModelScope.launch {
        try {
            _state.value = FeedModelState(refreshing = true)
            //repository.getAll()
            _state.value = FeedModelState()
        } catch (e: Exception) {
            _state.value = FeedModelState(error = true)
        }
    }
     */
}

@AssistedFactory
interface PostViewModelFactory {
    fun create(option: Boolean): PostViewModel
}