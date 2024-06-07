package com.example.nework.vm

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.example.nework.auth.AppAuth
import com.example.nework.dao.PostRemoteKeyDao
import com.example.nework.dto.FeedItem
import com.example.nework.dto.Post
import com.example.nework.repo.PostRepo
import com.example.nework.repo.PostRepoImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

@HiltViewModel(assistedFactory = PostByUserViewModelFactory::class)
@SuppressLint("CheckResult")//suppression warning
class PostByUserViewModel @AssistedInject constructor(
    private val repository: PostRepo,
    @Assisted private val userId: Int,
    private val appAuth: AppAuth,
) : ViewModel() {
    private val cached = repository
        .data
        .cachedIn(viewModelScope)

    val isActiveView = MutableLiveData<Boolean>(false)

    val dataById: Flow<PagingData<FeedItem>> = appAuth.authState
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { post ->
                    if (post is Post) {
                        post.copy(ownedByMe = post.authorId == myId)
                    } else {
                        post
                    }
                }.filter { (it is Post) && (it.authorId == userId) }
            }
        }
}

@AssistedFactory
interface PostByUserViewModelFactory {
    fun create(id: Int): PostByUserViewModel
}
