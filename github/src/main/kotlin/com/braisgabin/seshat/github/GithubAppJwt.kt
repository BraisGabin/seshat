package com.braisgabin.seshat.github

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dagger.Reusable
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@Reusable
class GithubAppJwtFactory(
    private val appId: String,
    privateKeyPem: ByteArray
) {
    @Inject
    constructor(
        @Named("githubAppId") appId: String,
        @Named("githubAppPem") privateKeyPemBase64: String
    ) : this(appId, Base64.getMimeDecoder().decode(privateKeyPemBase64))

    private val algorithm: Algorithm

    init {
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(privateKeyPem)) as RSAPrivateKey

        algorithm = Algorithm.RSA256(null, privateKey)
    }

    fun create(): String {
        val now = Instant.now()
        val expiration = now.plus(10, ChronoUnit.MINUTES)
        return JWT.create()
            .withClaim("iat", Date.from(now))
            .withClaim("exp", Date.from(expiration))
            .withClaim("iss", appId)
            .sign(algorithm)
    }
}
