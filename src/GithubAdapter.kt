package com.braisgabin.seshat

import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface GithubAdapter {

    @Headers("Accept: application/vnd.github.comfort-fade-preview+json")
    @POST("/repos/{owner}/{repo}/pulls/{pull_number}/comments")
    suspend fun addComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: String,
        @Body body: PrCommentBody
    ): Response<Unit>
}

data class PrCommentBody(
    val body: String,
    @Json(name = "commit_id") val commitId: String,
    val path: String,
    val side: String,
    val line: Int,
    @Json(name = "start_side") val startSide: String,
    @Json(name = "start_line") val startLine: Int
)

fun Side.toData(): String {
    return when (this) {
        Side.RIGHT -> "RIGHT"
        Side.LEFT -> "LEFT"
    }
}
