package com.nhom2.elearnlanguage.di

import android.content.Context
import com.nhom2.elearnlanguage.BuildConfig
import com.nhom2.elearnlanguage.data.source.local.TokenManager
import com.nhom2.elearnlanguage.data.source.remote.AuthDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.gson.gson
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideHttpClient(
        @ApplicationContext context: Context
    ): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                    setLenient()
                }
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30000L
                connectTimeoutMillis = 30000L
                socketTimeoutMillis = 30000L
            }
            defaultRequest {
                url(BuildConfig.API_BASE_URL)
                val token = TokenManager.getAccessToken(context)
                val url = this.url.build().toString()
                if (!url.contains("/api/auth/login") &&
                    !url.contains("/api/auth/register") &&
                    !url.contains("/api/auth/oauth2/google")
                ) {
                    token?.let {
                        header("Authorization", "Bearer $it")
                    }
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideAuthDataSource(client: HttpClient): AuthDataSource {
        return AuthDataSource(client)
    }

}