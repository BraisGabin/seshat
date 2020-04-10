package com.braisgabin.seshat.github

import com.braisgabin.seshat.entities.Suggestion
import javax.inject.Inject
import javax.inject.Named

class GithubUploadSuggestionsInteractor @Inject internal constructor(
    private val githubService: GithubService,
    private val githubAppJwtFactory: GithubAppJwtFactory,
    @Named("appUserName") private val appUserName: String
) {

    suspend fun invoke(
        installationId: String,
        owner: String,
        repo: String,
        pullNumber: String,
        commitId: String,
        suggestions: List<Suggestion>
    ) {
        val oauthToken = githubService.getOauthToken(
            installationId,
            githubAppJwtFactory.create(),
            "pull_requests" to "write"
        )

        val cleanSuccess = githubService.getCommentIdsFrom(owner, repo, pullNumber, appUserName, oauthToken)
            .map { commentId -> githubService.removeComment(owner, repo, commentId, oauthToken) }
            .all { it }

        check(cleanSuccess) { "Error cleaning the previous comment" }

        val success = suggestions
            .map { githubService.createComment(owner, repo, pullNumber, commitId, it, oauthToken) }
            .all { it }

        check(success) { "Error positing the suggestions" }
    }
}
