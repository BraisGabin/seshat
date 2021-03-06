package com.braisgabin.seshat

import com.braisgabin.seshat.github.DaggerGithubComponent
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DoubleReceive
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.request.receiveChannel
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.event.Level
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.net.URI
import java.security.Security
import java.security.MessageDigest.isEqual as secureEqual


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    Security.addProvider(BouncyCastleProvider())

    install(DoubleReceive) {
        receiveEntireContent = true
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(StatusPages) {
        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError)
            throw cause
        }
    }

    val json = Json(JsonConfiguration.Stable.copy(ignoreUnknownKeys = true))

    install(ContentNegotiation) {
        json(json = json)
    }

    val installationStorage = InstallationDataStorage(
        getPool(environment.config.property("ktor.redis.url").getString())
    )

    val signatureGenerator = SignatureGenerator(environment.config.property("ktor.github.app.webhook.secret").getString())

    val githubComponent = DaggerGithubComponent.factory()
        .create(
            okHttpClient = OkHttpClient()
                .newBuilder()
                .addNetworkInterceptor(
                    HttpLoggingInterceptor().apply {
                        setLevel(HttpLoggingInterceptor.Level.BODY)
                    })
                .build(),
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
                val signature = checkNotNull(call.request.header("X-Hub-Signature")) { "No signature!" }
                signatureGenerator.verify(call.receiveStream().readBytes(), signature)

                when (val eventName = call.request.header("X-GitHub-Event")!!) {
                    "integration_installation_repositories",
                    "installation_repositories" -> {
                        val event = call.receive<InstallationEvent>()
                        installationStorage[event.installation.account.login] = event.installation.id.toString()
                    }
                    "pull_request" -> {
                        val event = call.receive<PullRequestEvent>()
                        installationStorage[event.repository.owner.login] = event.installation.id.toString()
                    }
                    "check_suite" -> {
                        val event = call.receive<CheckSuitEvent>()
                        installationStorage[event.repository.owner.login] = event.installation.id.toString()
                    }
                    else -> error("Unknown event $eventName")
                }
                call.respond(HttpStatusCode.Created, "")
            }
        }
    }
}

fun SignatureGenerator.verify(payload: ByteArray, theirSignature: String) {
    val ourSignature = create(payload)
    check(secureEqual(ourSignature.toByteArray(), theirSignature.toByteArray())) { "Signatures didn't match!" }
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
