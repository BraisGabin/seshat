package com.braisgabin.seshat.github

import com.braisgabin.seshat.entities.Suggestion
import retrofit2.Response
import javax.inject.Inject

internal class GithubService @Inject internal constructor(
    private val githubAdapter: GithubAdapter
) {

    suspend fun createComment(
        owner: String,
        repo: String,
        pullNumber: String,
        commitId: String,
        comment: Suggestion,
        oauthToken: String
    ): Boolean {
        return githubAdapter.addComment(
            authorization = "token $oauthToken",
            owner = owner,
            repo = repo,
            pullNumber = pullNumber,
            body = comment.toData(commitId)
        ).isSuccessful
    }

    suspend fun getOauthToken(
        installationId: String,
        jwt: String,
        vararg permissions: Pair<String, String>
    ): String {
        val body = InstallationOauthRequest(permissions = permissions.toMap())
        return githubAdapter.getInstallationOauth("Bearer $jwt", installationId, body).body()!!.token
    }
}

private fun Suggestion.toData(commitId: String): PrCommentBody {
    return PrCommentBody(
        body = "```suggestion\n$code```",
        commitId = commitId,
        path = path,
        startSide = if (startLine == endLine) null else "RIGHT",
        startLine = if (startLine == endLine) null else startLine,
        side = "RIGHT",
        line = endLine
    )
}

fun Response<*>.getNextUrl(): String? {
    return headers()["Link"]?.let { header ->
        header.splitToSequence(",")
            .map {
                val link = it.split(";")
                link[0] to link[1]
            }
            .filter { (_, rel) -> rel.trim() == "rel=\"next\"" }
            .map { (url, _) -> url.trim().let { it.substring(1, it.length - 1) } }
            .firstOrNull()
    }
}
