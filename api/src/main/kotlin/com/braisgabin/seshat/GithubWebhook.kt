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
