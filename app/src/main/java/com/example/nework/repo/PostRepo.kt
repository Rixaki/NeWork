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
    fun getNewerCount(id: Long): Flow<Int>
    fun getPostById(id: Long): Post?
    suspend fun save(post: Post, upload: MediaUpload? = null)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun upload(upload: MediaUpload): Media
}