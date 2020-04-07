package com.braisgabin.seshat

import com.braisgabin.seshat.entities.Position
import com.braisgabin.seshat.entities.Side
import com.braisgabin.seshat.entities.Suggestion
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import io.reflectoring.diffparser.api.UnifiedDiffParser
import io.reflectoring.diffparser.api.model.Diff
import io.reflectoring.diffparser.api.model.Line
import java.io.InputStream

fun diffParser(channel: ByteReadChannel): List<Suggestion> {
    return parseDiff(channel.toInputStream())
        .flatMap { diff ->
            diff.hunks.map { hunk -> diff.toFileName.substring(2) to hunk }
        }
        .map { (path, hunk) ->
            val start = hunk.fromFileRange.lineStart
            val originalLines = hunk.lines
                .filter { line -> line.lineType != Line.LineType.TO }
            val first = originalLines.indexOfFirst { line -> line.lineType == Line.LineType.FROM }
            val last = originalLines.indexOfLast { line -> line.lineType == Line.LineType.FROM }
            val body = hunk.lines
                .filter { line -> line.lineType != Line.LineType.FROM }
                .dropWhile { line -> line.lineType != Line.LineType.TO }
                .dropLastWhile { line -> line.lineType != Line.LineType.TO }
                .joinToString("\n") { line -> line.content }
            Suggestion(
                path = path,
                start = Position(Side.RIGHT, start + first),
                end = Position(Side.RIGHT, start + last),
                code = "$body\n"
            )
        }
}

private fun parseDiff(inputStream: InputStream): List<Diff> {
    var diff: List<Diff>? = null
    inputStream.use {
        diff = UnifiedDiffParser().parse(inputStream)
    }
    return diff!!
}
