package com.braisgabin.seshat.entities

data class Suggestion(
    val path: String,
    val start: Position,
    val end: Position,
    val code: String
)

data class Position(
    val side: Side,
    val line: Int
)

enum class Side { LEFT, RIGHT }
