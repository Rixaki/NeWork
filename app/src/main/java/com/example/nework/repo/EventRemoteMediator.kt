package com.example.nework.repo

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.nework.api.AppApi
import com.example.nework.dao.EventDao
import com.example.nework.dao.EventRemoteKeyDao
import com.example.nework.db.EventDb
import com.example.nework.entity.EventEntity
import com.example.nework.entity.EventRemoteKeyEntity
import com.example.nework.error.ApiError
import java.lang.Math.max
import java.lang.Math.min

@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator(
    private val service: AppApi,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val eventDb: EventDb,
) : RemoteMediator<Int, EventEntity>() {
    override suspend fun initialize(): InitializeAction =
        if (eventDao.isEmpty()) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    if (eventDao.isEmpty()) {
                        service.getLatestEvent(state.config.pageSize)
                    } else {
                        val lastId = eventRemoteKeyDao.max()
                            ?: return MediatorResult.Success(false)
                        service.getAfterEvent(
                            lastId,
                            state.config.pageSize
                        )
                    }
                }

                LoadType.PREPEND -> {
                    //return MediatorResult.Success(endOfPaginationReached = true)

                    val lastId = eventRemoteKeyDao.max()
                        ?: return MediatorResult.Success(false)
                    service.getAfterEvent(
                        lastId,
                        state.config.pageSize
                    )

                }

                LoadType.APPEND -> {
                    val firstId = eventRemoteKeyDao.min()
                        ?: return MediatorResult.Success(false)
                    service.getBeforeEvent(
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
                    "EVENT: response body id-s range: ${body.firstOrNull()?.id} and" +
                            " ${body.lastOrNull()?.id}"
                )
                 */
            }

            eventDb.withTransaction {//all changes eventDao+keysDao or prev state
                when (loadType) {
                    LoadType.REFRESH -> {
                        println("trans refresh")
                        //eventDao.clear()//old version
                        eventRemoteKeyDao.insert(
                            listOf(
                                EventRemoteKeyEntity(
                                    EventRemoteKeyEntity.KeyType.AFTER,
                                    max(body.first().id, eventDao.max() ?: body.first().id)
                                ),
                                EventRemoteKeyEntity(
                                    EventRemoteKeyEntity.KeyType.BEFORE,
                                    min(body.last().id, eventDao.min() ?: body.last().id)
                                )
                            )
                        )
                    }

                    //UNREACHABLE WITHOUT AUTO_PREPEND
                    LoadType.PREPEND -> {
                        println("trans prepend")
                        eventRemoteKeyDao.insert(
                            EventRemoteKeyEntity(
                                EventRemoteKeyEntity.KeyType.AFTER,
                                body.first().id
                            )
                        )
                    }

                    LoadType.APPEND -> {
                        println("trans append")
                        eventRemoteKeyDao.insert(
                            EventRemoteKeyEntity(
                                EventRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id//maybe NoSuchElementException:List is empty.
                            )
                        )
                    }
                }

                eventDao.insert(body.map(EventEntity::fromDto))
            }

            //return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
            //reachable with only non-empty body
            return MediatorResult.Success(endOfPaginationReached = false)
        } catch (e: Exception) {
            if (!e.message.isNullOrBlank()) { println("ERROR MESSAGE: ${e.message}") }
            return MediatorResult.Error(e)
        }
    }
}