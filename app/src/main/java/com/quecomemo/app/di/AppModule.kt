package com.quecomemo.app.di

import com.quecomemo.app.data.repository.FirebaseSessionRepository
import com.quecomemo.app.domain.repository.SessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideSessionRepository(
        firebaseSessionRepository: FirebaseSessionRepository
    ): SessionRepository = firebaseSessionRepository
}
