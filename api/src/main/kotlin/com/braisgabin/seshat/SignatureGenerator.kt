package com.braisgabin.seshat

import org.bouncycastle.util.encoders.Hex
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class SignatureGenerator(
    secret: String
) {
    private val mac = Mac.getInstance("HmacSHA1").apply {
        init(SecretKeySpec(secret.toByteArray(), "RAW"))
    }

    fun create(data: ByteArray): String {
        val result = mac.doFinal(data)
        return "sha1=${Hex.encode(result).toString(Charsets.UTF_8)}"
    }
}
