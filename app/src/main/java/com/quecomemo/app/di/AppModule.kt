package com.quecomemo.app.di

import android.content.Context
import androidx.room.Room
import com.quecomemo.app.data.local.dao.FavoriteRecipeDao
import com.quecomemo.app.data.local.dao.RecipeDao
import com.quecomemo.app.data.local.database.QueComemoDatabase
import com.quecomemo.app.data.remote.ai.GeminiAssistantRepository
import com.quecomemo.app.data.repository.FirebaseAuthRepository
import com.quecomemo.app.data.repository.FirebaseSessionRepository
import com.quecomemo.app.data.repository.LocalFavoriteRepository
import com.quecomemo.app.data.repository.LocalRecipeRepository
import com.quecomemo.app.domain.repository.AssistantRepository
import com.quecomemo.app.domain.repository.AuthRepository
import com.quecomemo.app.domain.repository.FavoriteRepository
import com.quecomemo.app.domain.repository.RecipeRepository
import com.quecomemo.app.domain.repository.SessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun provideAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository
    ): AuthRepository = firebaseAuthRepository

    @Provides
    @Singleton
    fun provideSessionRepository(
        firebaseSessionRepository: FirebaseSessionRepository
    ): SessionRepository = firebaseSessionRepository

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): QueComemoDatabase {
        return Room.databaseBuilder(
            context,
            QueComemoDatabase::class.java,
            "quecomemo.db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideRecipeDao(database: QueComemoDatabase): RecipeDao = database.recipeDao()

    @Provides
    fun provideFavoriteRecipeDao(database: QueComemoDatabase): FavoriteRecipeDao =
        database.favoriteRecipeDao()

    @Provides
    @Singleton
    fun provideRecipeRepository(
        localRecipeRepository: LocalRecipeRepository
    ): RecipeRepository = localRecipeRepository

    @Provides
    @Singleton
    fun provideFavoriteRepository(
        localFavoriteRepository: LocalFavoriteRepository
    ): FavoriteRepository = localFavoriteRepository

    @Provides
    @Singleton
    fun provideAssistantRepository(
        geminiAssistantRepository: GeminiAssistantRepository
    ): AssistantRepository = geminiAssistantRepository
}
