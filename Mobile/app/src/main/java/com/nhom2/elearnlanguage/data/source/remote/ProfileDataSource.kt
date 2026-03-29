package com.nhom2.elearnlanguage.data.source.remote

import com.nhom2.elearnlanguage.BuildConfig
import com.nhom2.elearnlanguage.data.dto.ApiResponseDTO
import com.nhom2.elearnlanguage.data.dto.UserProfileDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject

class ProfileDataSource @Inject constructor(
    private val client: HttpClient
) {
    private val crlf = "\r\n"

    private fun sanitizeFileName(raw: String?): String {
        val fallback = "avatar.jpg"
        if (raw.isNullOrBlank()) return fallback
        val sanitized = raw
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .take(100)
        return if (sanitized.isBlank()) fallback else sanitized
    }

    private fun buildMultipartBody(
        boundary: String,
        fullName: String,
        avatarBytes: ByteArray?,
        avatarFileName: String?,
        avatarMimeType: String?
    ): ByteArray {
        val output = ByteArrayOutputStream()

        fun writeText(value: String) {
            output.write(value.toByteArray(StandardCharsets.UTF_8))
        }

        writeText("--$boundary$crlf")
        writeText("Content-Disposition: form-data; name=\"fullName\"$crlf$crlf")
        writeText(fullName)
        writeText(crlf)

        if (avatarBytes != null) {
            val safeName = sanitizeFileName(avatarFileName)
            val mime = avatarMimeType ?: "image/jpeg"
            writeText("--$boundary$crlf")
            writeText("Content-Disposition: form-data; name=\"avatar\"; filename=\"$safeName\"$crlf")
            writeText("Content-Type: $mime$crlf$crlf")
            output.write(avatarBytes)
            writeText(crlf)
        }

        writeText("--$boundary--$crlf")
        return output.toByteArray()
    }

    suspend fun getMyProfile(): ApiResponseDTO<UserProfileDTO> {
        return client.get("${BuildConfig.API_BASE_URL}/api/users/me/profile").body()
    }

    suspend fun updateMyProfile(
        fullName: String,
        avatarBytes: ByteArray?,
        avatarFileName: String?,
        avatarMimeType: String?
    ): ApiResponseDTO<UserProfileDTO> {
        val boundary = "----ELearnBoundary${UUID.randomUUID()}"
        val body = buildMultipartBody(
            boundary = boundary,
            fullName = fullName,
            avatarBytes = avatarBytes,
            avatarFileName = avatarFileName,
            avatarMimeType = avatarMimeType
        )

        return client.post("${BuildConfig.API_BASE_URL}/api/users/me/profile") {
            header(HttpHeaders.ContentType, "multipart/form-data; boundary=$boundary")
            setBody(body)
        }.body()
    }
}
