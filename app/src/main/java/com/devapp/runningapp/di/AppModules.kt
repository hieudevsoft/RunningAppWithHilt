package com.devapp.runningapp.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.devapp.runningapp.db.RunDao
import com.devapp.runningapp.db.RunDatabase
import com.devapp.runningapp.util.Constant
import com.devapp.runningapp.util.Constant.KEY_WEIGHT
import com.devapp.runningapp.util.SharedPreferenceHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Qualifier


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

    @Provides
    fun providePreferenceApp(@ApplicationContext context:Context): SharedPreferences = context.getSharedPreferences(Constant.PREFERENCE_NAME,MODE_PRIVATE)

    @Provides
    fun provideSharedPreferenceApp(@ApplicationContext context:Context): SharedPreferenceHelper = SharedPreferenceHelper(context)

    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getInt(KEY_WEIGHT, 80)
}

