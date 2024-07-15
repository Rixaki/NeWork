package com.example.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nework.dao.EventDao
import com.example.nework.dao.EventRemoteKeyDao
import com.example.nework.entity.BaseTypeConverter
import com.example.nework.entity.EventEntity
import com.example.nework.entity.EventRemoteKeyEntity

@Database(
    entities = [EventEntity::class, EventRemoteKeyEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(BaseTypeConverter::class)
abstract class EventDb : RoomDatabase() {
    abstract val eventDao: EventDao
    abstract val eventKeyDao: EventRemoteKeyDao
}