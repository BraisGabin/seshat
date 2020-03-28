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
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    val githubAdapter = Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create<GithubAdapter>()

    val githubService = GithubService(githubAdapter, environment.config.property("ktor.github.token").getString())

    routing {
        route("github") {
            post("{owner}/{repo}/pulls/{pull_number}/{commit_id}") {
                val diff = parse(call.receiveChannel())

                val owner: String = call.parameters["owner"]!!
                val repo: String = call.parameters["repo"]!!
                val pullNumber: String = call.parameters["pull_number"]!!
                val commitId: String = call.parameters["commit_id"]!!
                diff.forEach {
                    githubService.createComment(owner, repo, pullNumber, commitId, it)
                }
                call.respondText(diff.toString(), contentType = ContentType.Text.Plain)
            }
        }
    }
}
