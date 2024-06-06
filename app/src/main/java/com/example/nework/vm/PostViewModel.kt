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
import com.example.nework.repo.PostRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorJob = null,
    authorAvatar = null,
    content = "",
    published = "",
    coords = null,
    videoLink = null,
    likedByMe = false,
    attachment = null,
    mentionedMe = false
)

private val noPhoto = PhotoModel()
private val noList = emptyList<Int>()

@HiltViewModel
@SuppressLint("CheckResult")//suppression warning
class PostViewModel @Inject constructor(
    private val repository: PostRepo,
    private val remoteKeyDao: PostRemoteKeyDao,
    private val appAuth: AppAuth,
) : ViewModel() {
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

    fun getPostById(id: Int): Post {
        var result = empty
        cached.map { pagingData ->
            result = pagingData.filter { it is Post }.filter { post -> post.id == id } as Post
        }
        return result
    }

    private val _state = MutableLiveData<FeedModelState>()
    val state : LiveData<FeedModelState>
        get() = _state

    private val _coords = MutableLiveData<Coords?>()
    val coords : LiveData<Coords?>
        get() = _coords

    private val _list = MutableLiveData<List<Int>>()
    val list : LiveData<List<Int>>
        get() = _list

    val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated : LiveData<Unit>
        get() = _postCreated

    private val _postCancelled = SingleLiveEvent<Unit>()
    val postCancelled : LiveData<Unit>
        get() = _postCancelled

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

    fun clearModels() {
        edited.postValue(empty)
        _photo.postValue(noPhoto)
        _coords.postValue(null)
        _list.postValue(noList)
    }

    private fun load() = viewModelScope.launch {
        try {
            _state.value = FeedModelState(loading = true)
            //repository.stream.cachedIn(viewModelScope).
            _state.value = FeedModelState()
        } catch (e: Exception) {
            _state.value = FeedModelState(error = true)
        }
    }

    /*
    fun edit(post: Post) {
        edited.value = post
    }
     */

    fun cancelEdit() {
        edited.value = empty
        _postCancelled.postValue(Unit)
    }

    fun save() {
        edited.value?.let {
            viewModelScope.launch {
                try {
                    repository.save(
                        post = it,
                        upload = _photo.value?.uri?.let { MediaUpload(it.toFile()) }
                    )
                    _postCreated.value = Unit
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        clearModels()
    }

    fun changePostAndSave(content: String) {
        val isPostModelEmpty = (coords.value==null && list.value==noList)
        val oldPredicated = if (isPostModelEmpty)
            (edited.value?.content == content)
        else
            ((edited.value?.content == content) &&
            (edited.value?.coords == coords.value) &&
            (edited.value?.mentionIds == list.value))

        if (oldPredicated) {
            return
        }
        edited.value = if (isPostModelEmpty)
            edited.value?.copy(content = content)
        else
            edited.value?.copy(
                content = content,
                coords = coords.value,
                mentionIds = list.value ?: emptyList()
            )
        edited.value?.let { editedPost ->
            viewModelScope.launch {
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
                    _state.value = FeedModelState(
                        error = true,
                        lastErrorAction =
                        if (editedPost.id == 0)
                            "Error with add post."
                        else
                            "Error with edit post."
                    )
                    _postCancelled.postValue(Unit)
                }
            }
        }
        clearModels()
    }

    fun changePhoto(uri: Uri?) = _photo.postValue(PhotoModel(uri))
    fun changeCoords(coords: Coords? = null) = _coords.postValue(coords)
    fun changeMentionList(list: List<Int>) = _list.postValue(list)
    fun clearCoords() = _coords.postValue(null)
    fun clearPhoto() = _photo.postValue(noPhoto)
    fun clearMentionList() = _list.postValue(noList)

    fun likeById(id: Int) {
        viewModelScope.launch {
            val post = repository.getPostById(id)//antisticking before request answer (only with throw id, not post)
            if (post?.isLikeLoading == false) {
                try {
                    repository.likeById(id)//like and unlike in 1
                } catch (e: Exception) {
                    _state.value = FeedModelState(
                        error = true,
                        lastErrorAction = "Error with like/unlike post."
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
                    lastErrorAction = "Error with delete post."
                )
            }
        }
    }



    /*
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