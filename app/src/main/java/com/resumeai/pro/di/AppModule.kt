package com.resumeai.pro.di

import android.content.Context
import androidx.room.Room
import com.resumeai.pro.data.local.ResumeDao
import com.resumeai.pro.data.local.ResumeDatabase
import com.resumeai.pro.data.local.ThemePreferences
import com.resumeai.pro.data.repository.ResumeRepositoryImpl
import com.resumeai.pro.domain.repository.ResumeRepository
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
    fun provideResumeDatabase(@ApplicationContext context: Context): ResumeDatabase {
        return Room.databaseBuilder(
            context,
            ResumeDatabase::class.java,
            "resume_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideResumeDao(db: ResumeDatabase): ResumeDao = db.resumeDao

    @Provides
    @Singleton
    fun provideResumeRepository(dao: ResumeDao): ResumeRepository {
        return ResumeRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideThemePreferences(@ApplicationContext context: Context): ThemePreferences {
        return ThemePreferences(context)
    }
}
