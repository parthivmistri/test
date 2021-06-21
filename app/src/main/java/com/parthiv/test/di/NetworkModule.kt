package com.parthiv.test.di

import com.parthiv.test.network.DataAPI
import com.parthiv.test.network.MainRepository
import com.parthiv.test.network.MainRepositoryContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun getOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    @Provides
    @Singleton
    fun retrofitClient(baseURL: String, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder().client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseURL).build()

    @Provides
    @Singleton
    fun getMainRepositoryContract(): MainRepositoryContract = MainRepository(getApi())

    @Provides
    @Singleton
    fun getApi(): DataAPI =
        retrofitClient("https://www.google.com", getOkHttpClient()).create(DataAPI::class.java)
}