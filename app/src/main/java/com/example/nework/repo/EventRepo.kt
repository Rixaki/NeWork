package com.example.nework.repo

import androidx.paging.PagingData
import com.example.nework.dto.Event
import com.example.nework.dto.FeedItem
import com.example.nework.dto.Media
import com.example.nework.dto.MediaUpload
import kotlinx.coroutines.flow.Flow

interface EventRepo {
    val data: Flow<PagingData<FeedItem>>

    fun getNewerCount(id: Long): Flow<Int>
    suspend fun save(event: Event, upload: MediaUpload? = null)
    fun getEventById(id: Long): Event?
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun participateById(id: Long)
    suspend fun upload(upload: MediaUpload): Media
}