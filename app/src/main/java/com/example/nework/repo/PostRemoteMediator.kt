package com.example.nework.repo

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.nework.api.AppApi
import com.example.nework.dao.PostDao
import com.example.nework.dao.PostRemoteKeyDao
import com.example.nework.dao.WallByUserRemoteKeyDao
import com.example.nework.db.PostDb
import com.example.nework.entity.PostEntity
import com.example.nework.entity.PostRemoteKeyEntity
import com.example.nework.entity.WallByUserRemoteKeyEntity
import com.example.nework.error.ApiError
import java.lang.Math.max
import java.lang.Math.min

const val STARTING_PAGE_INDEX = 1

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val service: AppApi,
    private val postDao: PostDao,
    private val postKeyDao: PostRemoteKeyDao,
    private val wallKeyDao: WallByUserRemoteKeyDao,
    private val postDb: PostDb,
    private val isWall: Boolean = false,
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun initialize(): InitializeAction =
        if (postDao.isEmpty()) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        val keyDao = KeyDaoMediator(
            isWall = isWall,
            postKeyDao = postKeyDao,
            wallKeyDao = wallKeyDao
        )
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    if (postDao.isEmpty()) {
                        service.getLatestPost(state.config.pageSize)
                    } else {
                        val lastId = keyDao.max()
                            ?: return MediatorResult.Success(false)
                        service.getAfterPost(
                            lastId,
                            state.config.pageSize
                        )
                    }
                }

                LoadType.PREPEND -> {
                    //return MediatorResult.Success(endOfPaginationReached = true)

                    val lastId = keyDao.max()
                        ?: return MediatorResult.Success(false)
                    println("lastId $lastId")
                    service.getAfterPost(
                        lastId,
                        state.config.pageSize
                    )

                }

                LoadType.APPEND -> {
                    val firstId = keyDao.min()
                        ?: return MediatorResult.Success(false)
                    println("firstId $firstId")
                    service.getBeforePost(
                        firstId,
                        state.config.pageSize
                    )
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message(),
            )
            if (body.isEmpty()) {
                //println("empty response body")
                return MediatorResult.Success(endOfPaginationReached = true)
            } else {
                /*
                println(
                    "POST: response body id-s range: ${body.firstOrNull()?.id} and" +
                            " ${body.lastOrNull()?.id}"
                )
                 */
            }

            postDb.withTransaction { //all changes postdao+keysDao or prev state
                when (loadType) {
                    LoadType.REFRESH -> {
                        println("trans refresh")
                        //postDao.clear()//old version
                        keyDao.insert(
                            listOf(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    max(body.first().id, postDao.max() ?: body.first().id)
                                ),
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    min(body.last().id, postDao.min() ?: body.last().id)
                                )
                            )
                        )
                    }

                    //UNREACHABLE WITHOUT AUTO_PREPEND
                    LoadType.PREPEND -> {
                        println("trans prepend")
                        keyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id
                            )
                        )
                    }

                    LoadType.APPEND -> {
                        println("trans append")
                        keyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id//maybe NoSuchElementException:List is empty.
                            )
                        )
                    }
                }

                postDao.insert(body.map(PostEntity::fromDto))
            }

            //return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
            //reachable with only non-empty body
            return MediatorResult.Success(endOfPaginationReached = false)
        } catch (e: Exception) {
            println("ERROR MESSAGE: ${e.message}")
            return MediatorResult.Error(e)
        }
    }
}

@Suppress("UNCHECKED_CAST")
data class KeyDaoMediator(
    val isWall: Boolean = false,
    private val postKeyDao: PostRemoteKeyDao,
    private val wallKeyDao: WallByUserRemoteKeyDao,
) {
    suspend fun max(): Int? {
        return if (isWall) {
            wallKeyDao.max()
        } else {
            postKeyDao.max()
        }
    }

    suspend fun min(): Int? {
        return if (isWall) {
            wallKeyDao.min()
        } else {
            postKeyDao.min()
        }
    }

    suspend fun insert(ent: Any) {
        if (isWall) {
            wallKeyDao.insert(ent as WallByUserRemoteKeyEntity)
        } else {
            postKeyDao.insert(ent as PostRemoteKeyEntity)
        }
    }

    suspend fun insert(ent1: Any, ent2: Any) {
        if (isWall) {
            wallKeyDao.insert(listOf(ent1, ent2) as List<WallByUserRemoteKeyEntity>)
        } else {
            postKeyDao.insert(listOf(ent1, ent2) as List<PostRemoteKeyEntity>)
        }
    }
}