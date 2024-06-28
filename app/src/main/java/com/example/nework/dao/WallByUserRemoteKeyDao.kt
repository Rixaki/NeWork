package com.example.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nework.entity.WallByUserRemoteKeyEntity

@Dao
interface WallByUserRemoteKeyDao {
    @Query("SELECT max(id) FROM WallByUserRemoteKeyEntity")
    suspend fun max(): Long?

    @Query("SELECT min(id) FROM WallByUserRemoteKeyEntity")
    suspend fun min(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(postRemoteKeyEntity: WallByUserRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(postRemoteKeyEntity: List<WallByUserRemoteKeyEntity>)

    @Query("SELECT COUNT(*) == 0 FROM WallByUserRemoteKeyEntity")
    suspend fun isEmpty(): Boolean

    @Query("DELETE FROM WallByUserRemoteKeyEntity")
    suspend fun clear()
}