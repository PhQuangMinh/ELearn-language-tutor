package com.nhom2.elearnlanguage.di

import android.content.Context
import android.util.Log
import com.nhom2.elearnlanguage.BuildConfig
import com.nhom2.elearnlanguage.data.dto.ApiResponseDTO
import com.nhom2.elearnlanguage.data.dto.AuthResponseDTO
import com.nhom2.elearnlanguage.data.source.local.TokenManager
import com.nhom2.elearnlanguage.data.source.remote.AuthDataSource
import com.nhom2.elearnlanguage.data.source.remote.LessonDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.plugin
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.takeFrom
import io.ktor.http.content.TextContent
import io.ktor.serialization.gson.gson
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val HEADER_REFRESH_RETRY = "X-Refresh-Retry"
    private const val HEADER_SKIP_REFRESH = "X-Skip-Refresh"
    @Provides
    @Singleton
    fun provideHttpClient(
        @ApplicationContext context: Context
    ): HttpClient {
        val client = HttpClient {
            // Không follow redirect để tránh bị đá sang trang HTML (Google OAuth) rồi parse fail.
            followRedirects = false

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

            install(HttpSend) {
                // 1) call ban đầu, 2) call refresh, 3) retry lại call ban đầu
                maxSendCount = 3
            }
        }

        // Auto refresh access token khi gặp 401/302 (bị Spring Security redirect).
        client.plugin(HttpSend).intercept { request ->
            // Không refresh cho chính request refresh để tránh loop
            if (request.headers[HEADER_SKIP_REFRESH] == "1") {
                return@intercept execute(request)
            }

            val call = execute(request)
            val status = call.response.status

            val shouldAttemptRefresh =
                status == HttpStatusCode.Unauthorized ||
                    status == HttpStatusCode.Found ||
                    status == HttpStatusCode.SeeOther ||
                    status == HttpStatusCode.TemporaryRedirect ||
                    status == HttpStatusCode.PermanentRedirect

            val alreadyRetried = request.headers[HEADER_REFRESH_RETRY] == "1"
            if (!shouldAttemptRefresh || alreadyRetried) {
                return@intercept call
            }

            val refreshToken = TokenManager.getRefreshToken(context)
            if (refreshToken.isNullOrBlank()) {
                Log.e("AUTH_REFRESH", "Missing refresh token (status=$status)")
                return@intercept call
            }

            try {
                Log.d("AUTH_REFRESH", "Attempt refresh (status=$status)")
                val refreshRequest = HttpRequestBuilder().apply {
                    method = HttpMethod.Post
                    url.takeFrom("${BuildConfig.API_BASE_URL}/api/auth/refresh")
                    // Dùng OutgoingContent để tránh lỗi request transformation trong interceptor
                    setBody(TextContent("""{"refreshToken":"$refreshToken"}""", ContentType.Application.Json))
                    header(HEADER_SKIP_REFRESH, "1")
                }

                val refreshCall = execute(refreshRequest)
                if (refreshCall.response.status != HttpStatusCode.OK) {
                    Log.e("AUTH_REFRESH", "Refresh failed: ${refreshCall.response.status}")
                    return@intercept call
                }

                val refreshResponse: ApiResponseDTO<AuthResponseDTO> = refreshCall.body()
                if (!refreshResponse.success || refreshResponse.data == null) {
                    Log.e("AUTH_REFRESH", "Refresh failed: ${refreshResponse.message}")
                    return@intercept call
                }

                val newAccessToken = refreshResponse.data.token
                TokenManager.saveAccessToken(context, newAccessToken)
                TokenManager.saveRefreshToken(context, refreshResponse.data.refreshToken)
                Log.d("AUTH_REFRESH", "Refresh OK, retry request")

                // Retry request với access token mới
                request.headers.remove(HttpHeaders.Authorization)
                request.headers.append(HttpHeaders.Authorization, "Bearer $newAccessToken")
                request.headers.append(HEADER_REFRESH_RETRY, "1")
                execute(request)
            } catch (e: Exception) {
                Log.e("AUTH_REFRESH", "Refresh exception: ${e.message}", e)
                call
            }
        }

        return client
    }

    @Provides
    @Singleton
    fun provideAuthDataSource(client: HttpClient): AuthDataSource {
        return AuthDataSource(client)
    }

    @Provides
    @Singleton
    fun provideHomeDataSource(client: HttpClient): com.nhom2.elearnlanguage.data.source.remote.HomeDataSource {
        return com.nhom2.elearnlanguage.data.source.remote.HomeDataSource(client)
    }

    fun provideLessonDataSource(client: HttpClient): LessonDataSource {
        return LessonDataSource(client)
    }

}