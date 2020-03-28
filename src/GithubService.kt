package com.braisgabin.seshat

class GithubService(
    private val githubAdapter: GithubAdapter,
    private val oauthToken: String
) {

    suspend fun createComment(
        owner: String,
        repo: String,
        pullNumber: String,
        commitId: String,
        comment: PullRequestComment
    ): Boolean {
        return githubAdapter.addComment(
            authorization = "token $oauthToken",
            owner = owner,
            repo = repo,
            pullNumber = pullNumber,
            body = comment.toData(commitId)
        ).isSuccessful
    }
}

private fun PullRequestComment.toData(commitId: String): PrCommentBody {
    return PrCommentBody(
        body = "```suggestion\n$body```",
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
