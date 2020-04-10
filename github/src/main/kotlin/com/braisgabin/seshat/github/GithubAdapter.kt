package com.braisgabin.seshat.github

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

internal interface GithubAdapter {

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

    @GET("repos/{owner}/{repo}/pulls/{pull_number}/comments")
    suspend fun getComments(
        @Header("Authorization") authorization: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: String
    ): Response<List<CommentResponse>>

    @GET
    suspend fun getComments(
        @Header("Authorization") authorization: String,
        @Url url: String
    ): Response<List<CommentResponse>>

    @DELETE("repos/{owner}/{repo}/pulls/comments/{comment_id}")
    suspend fun removeComment(
        @Header("Authorization") authorization: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("comment_id") commentId: Long
    ): Response<List<CommentResponse>>
}

@JsonClass(generateAdapter = true)
internal data class AccessTokenResponse(
    val token: String
)

@JsonClass(generateAdapter = true)
internal data class InstallationOauthRequest(
    val permissions: Map<String, String>
)

@JsonClass(generateAdapter = true)
internal data class PrCommentBody(
    val body: String,
    @Json(name = "commit_id") val commitId: String,
    val path: String,
    val side: String,
    val line: Int,
    @Json(name = "start_side") val startSide: String?,
    @Json(name = "start_line") val startLine: Int?
)

@JsonClass(generateAdapter = true)
internal data class CommentResponse(
    val id: Long,
    val user: User
)

@JsonClass(generateAdapter = true)
internal data class User(
    val login: String
)
