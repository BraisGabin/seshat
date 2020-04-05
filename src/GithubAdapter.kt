package com.braisgabin.seshat

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

interface GithubAdapter {

    @Headers("Accept: application/vnd.github.comfort-fade-preview+json")
    @POST("repos/{owner}/{repo}/pulls/{pull_number}/comments")
    suspend fun addComment(
        @Header("Authorization") authorization: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: String,
        @Body body: PrCommentBody
    ): Response<Unit>

    @Headers("Accept: application/vnd.github.machine-man-preview+json")
    @POST("app/installations/{installation_id}/access_tokens")
    suspend fun getInstallationOauth(
        @Header("Authorization") authorization: String,
        @Path("installation_id") installationId: String,
        @Body body: InstallationOauthRequest
    ): Response<AccessTokenResponse>
}

@JsonClass(generateAdapter = true)
data class AccessTokenResponse(
    val token: String
)

@JsonClass(generateAdapter = true)
data class InstallationOauthRequest(
    val permissions: Map<String, String>
)

@JsonClass(generateAdapter = true)
data class PrCommentBody(
    val body: String,
    @Json(name = "commit_id") val commitId: String,
    val path: String,
    val side: String,
    val line: Int,
    @Json(name = "start_side") val startSide: String?,
    @Json(name = "start_line") val startLine: Int?
)
