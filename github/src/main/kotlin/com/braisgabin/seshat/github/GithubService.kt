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

    suspend fun getCommentIdsFrom(
        owner: String,
        repo: String,
        pullNumber: String,
        commenter: String,
        oauthToken: String
    ): List<Long> {
        val response = githubAdapter.getComments(
            authorization = "token $oauthToken",
            owner = owner,
            repo = repo,
            pullNumber = pullNumber
        )

        val list = mutableListOf<Long>()

        var (ids, nextUrl) = parse(response, commenter)
        list.addAll(ids)

        while (nextUrl != null) {
            githubAdapter.getComments("token $oauthToken", nextUrl)

            val (ids2, nextUrl2) = parse(response, commenter)
            list.addAll(ids2)
            nextUrl = nextUrl2
        }

        return ids
    }

    suspend fun removeComment(
        owner: String,
        repo: String,
        commentId: Long,
        oauthToken: String
    ): Boolean {
        return githubAdapter.removeComment(
            authorization = "token $oauthToken",
            owner = owner,
            repo = repo,
            commentId = commentId
        ).isSuccessful
    }

    private fun parse(response: Response<List<CommentResponse>>, commenter: String): Pair<List<Long>, String?> {
        val ids = response.body()!!
            .asSequence()
            .filter { it.user.login == commenter }
            .map { it.id }
            .toMutableList()

        val nextUrl = response.getNextUrl()

        return ids to nextUrl
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
