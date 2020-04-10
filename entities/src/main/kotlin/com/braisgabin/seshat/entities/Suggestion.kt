package com.braisgabin.seshat.entities

data class Suggestion(
    val path: String,
    val startLine: Int,
    val endLine: Int,
    val code: String
)
