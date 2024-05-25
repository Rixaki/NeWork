package com.example.nework.repo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepoImpl): PostRepo

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepoImpl): EventRepo
}