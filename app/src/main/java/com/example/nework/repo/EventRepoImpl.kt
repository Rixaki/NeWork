package com.example.nework.repo

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.nework.api.AppApi
import com.example.nework.dao.EventDao
import com.example.nework.dao.EventRemoteKeyDao
import com.example.nework.db.EventDb
import com.example.nework.dto.Attachment
import com.example.nework.dto.AttachmentType
import com.example.nework.dto.Event
import com.example.nework.dto.FeedItem
import com.example.nework.dto.Media
import com.example.nework.dto.MediaUpload
import com.example.nework.dto.TimeHeader
import com.example.nework.dto.TimeType
import com.example.nework.entity.EventEntity
import com.example.nework.error.ApiError
import com.example.nework.error.NetworkError
import com.example.nework.error.UnknownError
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepoImpl @Inject constructor(
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val appApi: AppApi,
    private val eventDb: EventDb
) : EventRepo {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(
            pageSize = 5,
            enablePlaceholders = false,
            prefetchDistance = 1
        ),
        pagingSourceFactory = { eventDao.pagingSource() },
        remoteMediator = EventRemoteMediator(
            service = appApi,
            eventDao = eventDao,
            eventRemoteKeyDao = eventRemoteKeyDao,
            eventDb = eventDb,
        )
    ).flow.map { pagingData ->
        pagingData.map(EventEntity::toDto)
            .insertSeparators { after: Event?, before: Event? ->
                if (after == null) {
                    return@insertSeparators TimeHeader(
                        id = 0, type = TimeType.TODAY
                    )
                }
                val curTime = System.currentTimeMillis() / 1000
                // old analog of Instant.now().epochSecond
                try {
                    val firstTime = before!!.toEpoch()//NPE throwable
                    val secondTime = after.toEpoch()//NPE throwable
                    //println("1st t: ${firstTime}, 2st t: ${secondTime},
                    // cur: " + "$curTime")
                    if ((curTime - firstTime < TODAY_COUNT)
                        &&
                        (curTime - secondTime >= TODAY_COUNT)
                    ) {
                        return@insertSeparators TimeHeader(
                            id = 0, type =
                            TimeType.YESTERDAY
                        )
                    } else {
                        if ((curTime - firstTime < WEEK_COUNT)
                            &&
                            (curTime - secondTime >= WEEK_COUNT)
                        ) {
                            return@insertSeparators TimeHeader(
                                id = 0, type =
                                TimeType.LAST_WEEK
                            )
                        } else {
                            return@insertSeparators null
                        }
                    }
                } catch (e: NullPointerException) {
                    return@insertSeparators null
                }
            }
    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = appApi.getNewerEvent(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body()
                ?: throw ApiError(response.code(), response.message())
            emit(body.size)
        }
    }.catch { flowOf(value = 0) }

    override fun getEventById(id: Long): Event? = eventDao.getEventById(id).toDto()

    override suspend fun save(event: Event, upload: MediaUpload?) {
        try {
            val eventWithAttachment = if (upload != null) {
                // TODO: check supporting for other types
                val media = upload(upload)//getting id after saving to dao
                event.copy(
                    attachment = Attachment(
                        url = media.url,
                        type = AttachmentType.IMAGE
                    )
                )
            } else event

            val response = appApi.saveEvent(eventWithAttachment)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message()
            )
            eventDao.insert(EventEntity.fromDto(body))

        } catch (e: Exception) {
            when (e) {
                is IOException -> {
                    throw NetworkError
                }

                else -> {
                    throw UnknownError
                }
            }
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            val response = appApi.deleteEventById(id)//api delete
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            } else {
                eventDao.removeById(id)//local delete
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> {
                    throw NetworkError
                }

                else -> {
                    throw UnknownError
                }
            }
        }
    }

    override suspend fun likeById(id: Long) {
        val eventEnt = eventDao.getEventById(id)
        try {
            eventDao.changeLikeLoadingById(id)
            val response = if (eventEnt.likedByMe) {
                appApi.unlikeEvent(id)//api unlike
            } else {
                appApi.likeEvent(id)//api like
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            } else {
                eventDao.likeEventById(id)//local like & unlike
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> {
                    throw NetworkError
                }

                else -> {
                    throw UnknownError
                }
            }
        } finally {
            //eventDao.insert(eventEnt.copy(isLikeLoading = false))
            eventDao.changeLikeLoadingById(id)
        }
    }

    override suspend fun participateById(id: Long) {
        val eventEnt = eventDao.getEventById(id)
        try {
            eventDao.insert(eventEnt.copy(isTakingPartLoading = true))
            val response = if (eventEnt.participatedByMe) {
                appApi.leaveEvent(id)
            } else {
                appApi.takePartEvent(id)
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            } else {
                //success api
                eventDao.takePartEventById(id)//local like & unlike
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> {
                    throw NetworkError
                }

                else -> {
                    throw UnknownError
                }
            }
        } finally {
            eventDao.insert(eventEnt.copy(isTakingPartLoading = false))
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media =
                MultipartBody.Part.createFormData(
                    "file", upload.file.name, upload.file.asRequestBody()
                )

            val response = appApi.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(
                response.code(),
                response.message()
            )
        } catch (e: Exception) {
            when (e) {
                is IOException -> {
                    throw NetworkError
                }

                else -> {
                    throw UnknownError
                }
            }
        }
    }
}
