package com.example.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nework.dao.WallByUserRemoteKeyDao
import com.example.nework.dao.WallDao
import com.example.nework.entity.BaseTypeConverter
import com.example.nework.entity.PostEntity
import com.example.nework.entity.PostRemoteKeyEntity

@Database(
    entities = [PostEntity::class, WallByUserRemoteKeyDao::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(BaseTypeConverter::class)
abstract class WallDb : RoomDatabase() {
    abstract val wallDao: WallDao
    abstract val wallKeyDao: WallByUserRemoteKeyDao

    companion object {
        @Volatile
        private var instance: WallDb? = null

        //buildDatabase migrated to DI
    }
}