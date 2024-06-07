package com.example.nework.repo

import androidx.paging.PagingData
import com.example.nework.dto.FeedItem
import com.example.nework.dto.Media
import com.example.nework.dto.MediaUpload
import com.example.nework.dto.Post
import kotlinx.coroutines.flow.Flow

interface PostRepo {
    var isWall: Boolean

    val data: Flow<PagingData<FeedItem>>

    //suspend fun getAll() //useless with paging
    fun getNewerCount(id: Int): Flow<Int>
    fun getPostById(id: Int): Post?
    suspend fun save(post: Post, upload: MediaUpload? = null)
    suspend fun removeById(id: Int)
    suspend fun likeById(id: Int)
    suspend fun upload(upload: MediaUpload): Media
}