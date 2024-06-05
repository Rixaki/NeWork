/*
package com.example.nework.repo

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.nework.api.AppApi
import com.example.nework.dao.WallByUserRemoteKeyDao
import com.example.nework.dao.WallDao
import com.example.nework.db.WallDb
import com.example.nework.entity.PostEntity
import com.example.nework.entity.WallByUserRemoteKeyEntity
import com.example.nework.error.ApiError
import java.lang.Math.max
import java.lang.Math.min

@OptIn(ExperimentalPagingApi::class)
class WallByUserRemoteMediator (
    private val service: AppApi,
    private val wallDao: WallDao,
    private val keyDao: WallByUserRemoteKeyDao,
    private val wallDb: WallDb,
    private val userId: Int,
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun initialize(): InitializeAction {
        wallDao.clear()
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }
        /*
        if (wallDao.isEmpty()) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
        */

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    if (wallDao.isEmpty()) {
                        service.getLatestWall(userId, state.config.pageSize)
                    } else {
                        val lastId = keyDao.max()
                            ?: return MediatorResult.Success(false)
                        service.getAfterWall(
                            authorId = userId,
                            postId = lastId,
                            count = state.config.pageSize
                        )
                    }
                }
                LoadType.PREPEND -> {
                    //return MediatorResult.Success(endOfPaginationReached = true)

                    val lastId = keyDao.max()
                        ?: return MediatorResult.Success(false)
                    service.getAfterWall(
                        authorId = userId,
                        postId = lastId,
                        count = state.config.pageSize
                    )

                }
                LoadType.APPEND -> {
                    val firstId = keyDao.min()
                        ?: return MediatorResult.Success(false)
                    service.getBeforeWall(
                        authorId = userId,
                        postId = firstId,
                        count = state.config.pageSize
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

            wallDb.withTransaction {//all changes postdao+keysDao or prev state
                when (loadType) {
                    LoadType.REFRESH -> {
                        println("trans refresh")
                        //postDao.clear()//old version
                        keyDao.insert(
                            listOf(
                                WallByUserRemoteKeyEntity(
                                    WallByUserRemoteKeyEntity.KeyType.AFTER,
                                    max(body.first().id, wallDao.max() ?: body.first().id)
                                ),
                                WallByUserRemoteKeyEntity(
                                    WallByUserRemoteKeyEntity.KeyType.BEFORE,
                                    min(body.last().id, wallDao.min() ?: body.last().id)
                                )
                            )
                        )
                    }

                    //UNREACHABLE WITHOUT AUTO_PREPEND
                    LoadType.PREPEND -> {
                        println("trans prepend")
                        keyDao.insert(
                            WallByUserRemoteKeyEntity(
                                WallByUserRemoteKeyEntity.KeyType.AFTER,
                                body.first().id
                            )
                        )
                    }

                    LoadType.APPEND -> {
                        println("trans append")
                        keyDao.insert(
                            WallByUserRemoteKeyEntity(
                                WallByUserRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id//maybe NoSuchElementException:List is empty.
                            )
                        )
                    }
                }

                wallDao.insert(body.map(PostEntity::fromDto))
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
 */