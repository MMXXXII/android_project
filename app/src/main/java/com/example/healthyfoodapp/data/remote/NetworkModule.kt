package com.example.healthyfoodapp.data.remote

import android.content.Context
import android.net.ConnectivityManager
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

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected == true
    }

    fun provideOkHttpClient(context: Context): OkHttpClient {
        val cache = Cache(File(context.cacheDir, "http_cache"), CACHE_SIZE)
        return OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .cache(cache)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = if (isNetworkAvailable(context)) {
                    chain.request().newBuilder()
                        .header("Cache-Control", "public, max-age=86400").build()
                } else {
                    chain.request().newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=${60 * 60 * 24 * 7}").build()
                }
                chain.proceed(request)
            }
            .addNetworkInterceptor { chain ->
                chain.proceed(chain.request()).newBuilder()
                    .header("Cache-Control", "public, max-age=86400").build()
            }
            .build()
    }

    fun provideMealApiService(context: Context): MealApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealApiService::class.java)
    }
}