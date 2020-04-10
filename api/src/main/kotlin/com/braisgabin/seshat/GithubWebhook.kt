package com.braisgabin.seshat

import kotlinx.serialization.Serializable

@Serializable
class InstallationEvent(
    val installation: Installation
)

@Serializable
class Installation(
    val id: Long,
    val account: Account
)

@Serializable
class Account(
    val login: String
)

@Serializable
class PullRequestEvent(
    val repository: Repository,
    val installation: InstallationId
)

@Serializable
class CheckSuitEvent(
    val repository: Repository,
    val installation: InstallationId
)

@Serializable
class Repository(
    val owner: Account
)

@Serializable
class InstallationId(
    val id: Long
)
