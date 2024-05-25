package com.example.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nework.dao.UserDao
import com.example.nework.entity.BaseTypeConverter
import com.example.nework.entity.UserEntity
import com.example.nework.entity.WallByUserRemoteKeyEntity

@Database(
    entities = [UserEntity::class, WallByUserRemoteKeyEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(BaseTypeConverter::class)
abstract class UserDb : RoomDatabase() {
    abstract val userDao: UserDao

    companion object {
        @Volatile
        private var instance: UserDb? = null

        //buildDatabase migrated to DI
    }
}