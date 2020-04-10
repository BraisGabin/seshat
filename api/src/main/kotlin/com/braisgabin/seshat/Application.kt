package com.braisgabin.seshat

import com.braisgabin.seshat.github.DaggerGithubComponent
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.request.receiveChannel
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.OkHttpClient
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.event.Level
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.net.URI
import java.security.Security


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    Security.addProvider(BouncyCastleProvider())

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    val json = Json(JsonConfiguration.Stable.copy(ignoreUnknownKeys = true))

    install(ContentNegotiation) {
        json(json = json)
    }

    val installationStorage = InstallationDataStorage(
        getPool(environment.config.property("ktor.redis.url").getString())
    )

    val githubComponent = DaggerGithubComponent.factory()
        .create(
            okHttpClient = OkHttpClient(),
            githubAppId = environment.config.property("ktor.github.app.id").getString(),
            githubAppPem = environment.config.property("ktor.github.app.pem").getString()
        )

    val githubUploadSuggestionsInteractor = githubComponent.githubUploadSuggestionsInteractor()

    routing {
        route("github") {
            post("{owner}/{repo}/pulls/{pull_number}/{commit_id}") {
                val suggestions = diffParser(call.receiveChannel())

                val owner: String = call.parameters["owner"]!!
                val repo: String = call.parameters["repo"]!!
                val pullNumber: String = call.parameters["pull_number"]!!
                val commitId: String = call.parameters["commit_id"]!!

                val installationId: String = installationStorage[owner]

                githubUploadSuggestionsInteractor.invoke(installationId, owner, repo, pullNumber, commitId, suggestions)
                call.respondText(suggestions.toString(), contentType = ContentType.Text.Plain)
            }
        }
        route("webhook") {
            post("github") {
                val installation = call.receive<InstallationEvent>().installation
                installationStorage[installation.account.login] = installation.id.toString()
                call.respond(HttpStatusCode.NoContent, "")
            }
        }
    }
}

private fun getPool(url: String): JedisPool {
    val redisURI = URI(url)
    val poolConfig = JedisPoolConfig().apply {
        maxTotal = 10
        maxIdle = 5
        minIdle = 1
        testOnBorrow = true
        testOnReturn = true
        testWhileIdle = true
    }
    return JedisPool(poolConfig, redisURI)
}
