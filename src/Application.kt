package com.braisgabin.seshat

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
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
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.event.Level
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.net.URI
import java.security.KeyFactory
import java.security.Security
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*


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

    val jedisPool = getPool()

    val githubAdapter = Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create<GithubAdapter>()

    val githubService = GithubService(githubAdapter)

    val githubAppId = environment.config.property("ktor.github.app.id").getString()
    val githubAppPem = environment.config.property("ktor.github.app.pem").getString()
    val simpleJWT = SimpleJWT(githubAppPem)

    routing {
        route("github") {
            post("{owner}/{repo}/pulls/{pull_number}/{commit_id}") {
                val diff = parse(call.receiveChannel())

                val owner: String = call.parameters["owner"]!!
                val repo: String = call.parameters["repo"]!!
                val pullNumber: String = call.parameters["pull_number"]!!
                val commitId: String = call.parameters["commit_id"]!!

                val installationId: String = jedisPool.getResource().use { jedis ->
                    jedis.get(owner)
                }
                val oauthToken = githubService.getOauthToken(installationId, simpleJWT.sign(githubAppId))
                diff.forEach {
                    githubService.createComment(owner, repo, pullNumber, commitId, it, oauthToken)
                }
                call.respondText(diff.toString(), contentType = ContentType.Text.Plain)
            }
        }
        route("webhook") {
            post("github") {
                val installation = call.receive<InstallationEvent>().installation
                jedisPool.getResource().use { jedis ->
                    jedis.set(installation.account.login.toLowerCase(), installation.id.toString())
                }
                call.respond(HttpStatusCode.NoContent, "")
            }
        }
    }
}

private fun getPool(): JedisPool {
    val redisURI = URI(System.getenv("REDIS_URL"))
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

class SimpleJWT(privateKeyPem: ByteArray) {
    constructor(privateKeyPemBase64: String) : this(Base64.getMimeDecoder().decode(privateKeyPemBase64))

    private val algorithm: Algorithm

    init {
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(privateKeyPem)) as RSAPrivateKey

        algorithm = Algorithm.RSA256(null, privateKey)
    }

    fun sign(appId: String): String {
        val now = Instant.now()
        val expiration = now.plus(10, ChronoUnit.MINUTES)
        return JWT.create()
            .withClaim("iat", Date.from(now))
            .withClaim("exp", Date.from(expiration))
            .withClaim("iss", appId)
            .sign(algorithm)
    }
}
