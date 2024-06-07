package com.example.nework.repo

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.room.withTransaction
import com.example.nework.api.AppApi
import com.example.nework.dao.PostDao
import com.example.nework.dao.PostRemoteKeyDao
import com.example.nework.dao.WallByUserRemoteKeyDao
import com.example.nework.db.PostDb
import com.example.nework.dto.Ad
import com.example.nework.dto.Attachment
import com.example.nework.dto.AttachmentType
import com.example.nework.dto.FeedItem
import com.example.nework.dto.Media
import com.example.nework.dto.MediaUpload
import com.example.nework.dto.Post
import com.example.nework.dto.TimeHeader
import com.example.nework.dto.TimeType
import com.example.nework.entity.PostEntity
import com.example.nework.error.*
import kotlinx.coroutines.coroutineScope
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

const val TODAY_COUNT : Long = 24 * 60 * 60
const val WEEK_COUNT : Long = 48 * 60 * 60

@Singleton
class PostRepoImpl @Inject constructor(
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val wallRemoteKeyDao: WallByUserRemoteKeyDao,
    private val appApi: AppApi,
    private val postDb: PostDb,
) : PostRepo {
    override var isWall: Boolean = false

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(
            pageSize = 5,
            enablePlaceholders = false,
            prefetchDistance = 1
        ),
        pagingSourceFactory = { postDao.pagingSource() },
        remoteMediator = PostRemoteMediator(
            service = appApi,
            postDao = postDao,
            postKeyDao = postRemoteKeyDao,
            wallKeyDao = wallRemoteKeyDao,
            postDb = postDb,
            isWall = isWall
        )
    ).flow.map { pagingData ->
        pagingData
            .map(PostEntity::toDto)
            //TimeHeader adding
            .insertSeparators { after: Post?, before: Post? ->
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
            //AD adding
            .insertSeparators { before: FeedItem?, _ ->
                if (before?.id?.rem(5) == 0 && before !is TimeHeader) {
                    return@insertSeparators Ad(0, "figma.jpg")
                } else {
                    return@insertSeparators null
                }
            }
    }

    override fun getNewerCount(id: Int): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = appApi.getNewerPost(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body()
                ?: throw ApiError(response.code(), response.message())
            emit(body.size)
        }
    }.catch { flowOf(value = 0) }

    override fun getPostById(id: Int): Post? = postDao.getPostById(id).toDto()

    override suspend fun save(
        post: Post,
        upload: MediaUpload?
    ) {
        try {
            val postWithAttachment = if (upload != null) {
                // TODO: check supporting for other types
                val media = upload(upload)//getting id after saving to dao
                post.copy(
                    attachment = Attachment(
                        url = media.url,
                        type = AttachmentType.IMAGE
                    )
                )
            } else post

            val response = appApi.savePost(postWithAttachment)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message()
            )
            postDao.insert(PostEntity.fromDto(body))

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

    override suspend fun removeById(id: Int) {
        try {
            postDb.withTransaction {
                val response = appApi.deletePostById(id)//api delete
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                } else {
                    postDao.removeById(id)//local delete
                }
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

    override suspend fun likeById(id: Int) {
        val postEnt = postDao.getPostById(id)
        try {
            //postDao.insert(postEnt.copy(isLikeLoading = true))
            postDao.changeLikeLoadingById(id)
            val response = if (postEnt.likedByMe) {
                appApi.unlikePost(id)
            } else {
                appApi.likePost(id)
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            } else {
                //success api
                postDao.likePostById(id)//local like & unlike
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
            //postDao.insert(postEnt.copy(isLikeLoading = false))
            postDao.changeLikeLoadingById(id)
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

    /*
        //old version
        override suspend fun upload(upload: MediaUpload): Media =
        appApi.upload(
            MultipartBody.Part.createFormData(
                "file",
                upload.file.name,
                upload.file.asRequestBody()
            )
        )
        */
}