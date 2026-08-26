plugins {
    kotlin("jvm") version "2.3.0" apply false
    kotlin("plugin.spring") version "2.3.0" apply false
    kotlin("plugin.jpa") version "2.3.0" apply false

    id("org.springframework.boot") version "4.1.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

val envFile = file(".env")

val envVariables = if(envFile.exists()) {
    envFile.readLines()
        .filter { it.isNotBlank() && !it.trim().startsWith("#") }
        .associate {
            val (key, value) = it.split("=", limit = 2)
            key.trim() to value.trim()
        }
} else {
    emptyMap()
}

subprojects {
    tasks.withType<JavaExec>().configureEach {
        environment(envVariables)
    }
}