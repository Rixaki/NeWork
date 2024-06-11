package com.example.nework.db

import android.content.Context
import androidx.room.Room
import com.example.nework.dao.EventDao
import com.example.nework.dao.EventRemoteKeyDao
import com.example.nework.dao.PostDao
import com.example.nework.dao.PostRemoteKeyDao
import com.example.nework.dao.UserDao
import com.example.nework.dao.WallByUserRemoteKeyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)//depen-t for all app
@Module
object DbModule {
    @Singleton//set lifecycle
    @Provides
    fun providePostDb(
        @ApplicationContext
        context: Context//uses cxt whole app
    ): PostDb =
        Room.databaseBuilder(
            context,
            PostDb::class.java,
            "post_db.db"
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()

    @Provides
    fun providePostDao(
        postDb: PostDb
    ) : PostDao = postDb.postDao

    @Provides
    fun providePostKeyDao(
        postDb: PostDb
    ) : PostRemoteKeyDao = postDb.postKeyDao

    @Provides
    fun provideWallKeyDao(
        postDb: PostDb
    ) : WallByUserRemoteKeyDao = postDb.wallKeyDao
//_____________________________________________
    @Singleton
    @Provides
    fun provideEventDb(
        @ApplicationContext
        context: Context//uses cxt whole app
    ): EventDb =
        Room.databaseBuilder(
            context,
            EventDb::class.java,
            "event_db.db"
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()

    @Provides
    fun provideEventDao(
        eventDb: EventDb
    ) : EventDao = eventDb.eventDao

    @Provides
    fun provideEventKeyDao(
        eventDb: EventDb
    ) : EventRemoteKeyDao = eventDb.eventKeyDao
//_____________________________________________
    @Singleton
    @Provides
    fun provideUserDb(
        @ApplicationContext
        context: Context//uses cxt whole app
    ): UserDb =
        Room.databaseBuilder(
            context,
            UserDb::class.java,
            "user_db.db"
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()

    @Provides
    fun provideUserDao(
        userDb: UserDb
    ) : UserDao = userDb.userDao
}