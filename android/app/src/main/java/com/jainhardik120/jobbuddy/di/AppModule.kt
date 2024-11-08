package com.jainhardik120.jobbuddy.di

import android.content.Context
import com.jainhardik120.jobbuddy.R
import com.jainhardik120.jobbuddy.data.remote.JobBuddyAPI
import com.jainhardik120.jobbuddy.data.remote.JobBuddyAPIImpl
import com.jainhardik120.jobbuddy.data.KeyValueStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(OkHttp) {
            BrowserUserAgent()
            expectSuccess = true
            install(Logging) {
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 50000
            }
        }
    }


    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): KeyValueStorage {
        return KeyValueStorage(
            context.getSharedPreferences(
                context.resources.getString(R.string.app_name),
                Context.MODE_PRIVATE
            )
        )
    }


    @Provides
    @Singleton
    fun provideApiImpl(client: HttpClient, keyValueStorage: KeyValueStorage): JobBuddyAPI {
        return JobBuddyAPIImpl(client, keyValueStorage)
    }



}