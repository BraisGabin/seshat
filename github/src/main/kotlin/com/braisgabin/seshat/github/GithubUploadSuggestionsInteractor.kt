package com.braisgabin.seshat.github

import com.braisgabin.seshat.entities.Suggestion
import javax.inject.Inject

class GithubUploadSuggestionsInteractor @Inject internal constructor(
    private val githubService: GithubService,
    private val githubAppJwtFactory: GithubAppJwtFactory
) {

    suspend fun invoke(
        installationId: String,
        owner: String,
        repo: String,
        pullNumber: String,
        commitId: String,
        suggestions: List<Suggestion>
    ) {
        val oauthToken = githubService.getOauthToken(installationId, githubAppJwtFactory.create())
        suggestions.forEach {
            githubService.createComment(owner, repo, pullNumber, commitId, it, oauthToken)
        }
    }
}
