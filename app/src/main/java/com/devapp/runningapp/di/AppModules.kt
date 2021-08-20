package com.devapp.runningapp.di

import android.content.Context
import com.devapp.runningapp.db.RunDao
import com.devapp.runningapp.db.RunDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object AppModules {

    @Provides
    fun provideRunDatabase(@ApplicationContext context: Context):RunDatabase {
        return RunDatabase.invoke(context)
    }

    @Provides
    fun provideRunDao(database: RunDatabase):RunDao{
        return database.getRunDao()
    }


}