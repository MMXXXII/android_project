package com.example.healthyfoodapp.network

import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object NetworkModule {

    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"
    private const val CACHE_SIZE = 10L * 1024 * 1024

    fun provideOkHttpClient(cacheDir: File): OkHttpClient {
        val cache = Cache(File(cacheDir, "http_cache"), CACHE_SIZE)

        return OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .cache(cache)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Cache-Control", "public, max-age=60")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    fun provideRetrofit(cacheDir: File): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttpClient(cacheDir))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun provideMealApiService(cacheDir: File): MealApiService {
        return provideRetrofit(cacheDir).create(MealApiService::class.java)
    }
}