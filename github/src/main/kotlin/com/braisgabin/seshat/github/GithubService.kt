package com.braisgabin.seshat.github

import com.braisgabin.seshat.entities.Side
import com.braisgabin.seshat.entities.Suggestion
import javax.inject.Inject

class GithubService @Inject internal constructor(
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
        jwt: String
    ): String {
        val body = InstallationOauthRequest(permissions = mapOf("pull_requests" to "write"))
        return githubAdapter.getInstallationOauth("Bearer $jwt", installationId, body).body()!!.token
    }
}

private fun Suggestion.toData(commitId: String): PrCommentBody {
    return PrCommentBody(
        body = "```suggestion\n$code```",
        commitId = commitId,
        path = path,
        startSide = if (start == end) null else start.side.toData(),
        startLine = if (start == end) null else start.line,
        side = end.side.toData(),
        line = end.line
    )
}

private fun Side.toData(): String {
    return when (this) {
        Side.RIGHT -> "RIGHT"
        Side.LEFT -> "LEFT"
    }
}
