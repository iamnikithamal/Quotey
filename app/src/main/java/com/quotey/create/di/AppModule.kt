package com.quotey.create.di

import android.content.Context
import com.quotey.create.data.preferences.PreferencesManager
import com.quotey.create.data.repository.ProjectRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideProjectRepository(
        @ApplicationContext context: Context
    ): ProjectRepository {
        return ProjectRepository(context)
    }
}
