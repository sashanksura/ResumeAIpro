package com.resumeai.pro.di

import com.resumeai.pro.data.api.AIConfig
import com.resumeai.pro.data.api.AIService
import com.resumeai.pro.data.api.JobDescriptionExtractor
import com.resumeai.pro.data.api.NvidiaApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (com.resumeai.pro.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.HEADERS
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AIConfig.endpoint)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNvidiaApiService(retrofit: Retrofit): NvidiaApiService {
        return retrofit.create(NvidiaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAIService(apiService: NvidiaApiService): AIService {
        return AIService(apiService)
    }

    @Provides
    @Singleton
    fun provideJobDescriptionExtractor(
        okHttpClient: OkHttpClient,
        aiService: AIService
    ): JobDescriptionExtractor {
        return JobDescriptionExtractor(okHttpClient, aiService)
    }
}
