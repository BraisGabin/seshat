plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("com.squareup.okhttp3:okhttp-bom:4.4.1"))
    api(platform("io.ktor:ktor-bom:1.3.2"))

    constraints {
        api("ch.qos.logback:logback-classic:1.2.1")
        api("io.reflectoring.diffparser:diffparser:1.4")
        api("com.squareup.retrofit2:retrofit:2.8.1")
        api("com.squareup.retrofit2:converter-moshi:2.8.1")
        api("org.bouncycastle:bcprov-jdk15on:1.64")
        api("com.auth0:java-jwt:3.10.2")
        api("redis.clients:jedis:3.2.0")
        api("com.squareup.moshi:moshi-kotlin:1.9.2")
        api("com.squareup.moshi:moshi-kotlin-codegen:1.9.2")
        api("com.google.dagger:dagger:2.27")
        api("com.google.dagger:dagger:2.27")
        api("com.google.dagger:dagger-compiler:2.27")
    }
}
