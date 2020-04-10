package com.braisgabin.seshat

import redis.clients.jedis.JedisPool

class InstallationDataStorage(
    private val jedisPool: JedisPool
) {

    operator fun get(key: String): String {
        return jedisPool.getResource().use { jedis ->
            jedis.get(key)
        }
    }

    operator fun set(key: String, value: String) {
        jedisPool.getResource().use { jedis ->
            jedis.set(key, value)
        }
    }
}
