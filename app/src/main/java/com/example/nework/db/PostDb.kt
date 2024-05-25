package com.example.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nework.dao.PostDao
import com.example.nework.dao.PostRemoteKeyDao
import com.example.nework.entity.BaseTypeConverter
import com.example.nework.entity.PostEntity
import com.example.nework.entity.PostRemoteKeyEntity

@Database(
    entities = [PostEntity::class, PostRemoteKeyEntity::class],
    version = 1,
    exportSchema = false,
    )
@TypeConverters(BaseTypeConverter::class)
abstract class PostDb : RoomDatabase() {
    abstract val postDao: PostDao
    abstract val postKeyDao: PostRemoteKeyDao

    companion object {
        @Volatile
        private var instance: PostDb? = null

        //buildDatabase migrated to DI
    }
}