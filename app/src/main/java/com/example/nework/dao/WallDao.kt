/*
package com.example.nework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nework.entity.PostEntity

@Dao
interface WallDao {
    @Query("SELECT * FROM WallPostEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, PostEntity>
    //pagingsource autogenerated by room
    //manual create pagingSource NOT! recommended

    @Query("SELECT * FROM WallPostEntity WHERE id = :id")
    fun getPostById(id: Int): PostEntity

    @Query("SELECT COUNT(*) FROM WallPostEntity")
    fun getSize(): Int

    @Query("DELETE FROM WallPostEntity WHERE id = :id")
    suspend fun removeById(id: Int)

    @Query(
        """
                UPDATE WallPostEntity SET
                    likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
                    likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
                WHERE id = :id;
            """
    )
    suspend fun likePostById(id: Int)

    @Query(
        """
                UPDATE WallPostEntity SET
                    isLikeLoading = CASE WHEN isLikeLoading THEN 0 ELSE 1 END
                WHERE id = :id;
            """
    )
    suspend fun changeLikeLoadingById(id: Int)

    //for paging
    @Query("SELECT COUNT(*) == 0 FROM WallPostEntity")
    suspend fun isEmpty(): Boolean

    //for paging
    @Query("SELECT max(id) FROM WallPostEntity")
    suspend fun max():Int?

    //for paging
    @Query("SELECT min(id) FROM WallPostEntity")
    suspend fun min():Int?

    @Query("DELETE FROM WallPostEntity")
    suspend fun clear()
}
 */