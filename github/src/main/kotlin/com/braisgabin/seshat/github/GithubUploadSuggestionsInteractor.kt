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

        githubService.getCommentIdsFrom(owner, repo, pullNumber, appUserName, oauthToken)
            .forEach { githubService.removeComment(owner, repo, it, oauthToken) }

        suggestions.forEach {
            githubService.createComment(owner, repo, pullNumber, commitId, it, oauthToken)
        }
    }
}
