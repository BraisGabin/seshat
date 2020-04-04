package com.braisgabin.seshat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GithubWebhookData(
    val repository: Repository,
    val installation: Installation
)

@Serializable
class Repository(
    @SerialName("full_name") val fullName: String
)

@Serializable
class Installation(
    val id: Long
)
