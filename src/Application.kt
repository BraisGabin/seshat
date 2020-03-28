package com.braisgabin.seshat

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.http.ContentType
import io.ktor.request.path
import io.ktor.request.receiveChannel
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    routing {
        route("github") {
            post("{user}/{repository}/pulls/{pr}") {
                val diff = parse(call.receiveChannel())

                call.respondText(diff.toString(), contentType = ContentType.Text.Plain)
            }
        }
    }
}
