package com.braisgabin.seshat

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class GithubWebhookData(
    val repository: Repository,
    val installation: Installation
)

@JsonClass(generateAdapter = true)
class Repository(
    @Json(name = "full_name") val fullName: String
)

@JsonClass(generateAdapter = true)
class Installation(
    val id: String
)
