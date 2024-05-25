package com.example.nework.repo

import androidx.paging.PagingData
import com.example.nework.dto.Event
import com.example.nework.dto.FeedItem
import com.example.nework.dto.Media
import com.example.nework.dto.MediaUpload
import com.example.nework.dto.Post
import kotlinx.coroutines.flow.Flow

interface EventRepo {
    val data: Flow<PagingData<FeedItem>>

    fun getNewerCount(id: Int): Flow<Int>
    suspend fun save(event: Event, upload: MediaUpload? = null)
    fun getEventById(id: Int): Event?
    suspend fun removeById(id: Int)
    suspend fun likeById(id: Int)
    suspend fun participateById(id: Int)
    suspend fun upload(upload: MediaUpload): Media
}