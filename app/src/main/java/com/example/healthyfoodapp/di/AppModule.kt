package com.example.healthyfoodapp.di

import android.content.Context
import com.example.healthyfoodapp.data.local.DBHelper
import com.example.healthyfoodapp.data.remote.MealApiService
import com.example.healthyfoodapp.data.remote.NetworkModule
import com.example.healthyfoodapp.data.repository.DishRepositoryImpl
import com.example.healthyfoodapp.data.repository.MealRepositoryImpl
import com.example.healthyfoodapp.domain.repository.DishRepository
import com.example.healthyfoodapp.domain.repository.MealRepository
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
    fun provideDBHelper(@ApplicationContext context: Context): DBHelper =
        DBHelper(context)

    @Provides
    @Singleton
    fun provideMealApiService(@ApplicationContext context: Context): MealApiService =
        NetworkModule.provideMealApiService(context)

    @Provides
    @Singleton
    fun provideDishRepository(dbHelper: DBHelper): DishRepository =
        DishRepositoryImpl(dbHelper)

    @Provides
    @Singleton
    fun provideMealRepository(apiService: MealApiService): MealRepository =
        MealRepositoryImpl(apiService)
}