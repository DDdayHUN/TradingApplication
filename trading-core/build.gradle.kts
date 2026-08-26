plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")

    application

    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "app.trading"
version = "1.0-SNAPSHOT"

repositories { mavenCentral() }
kotlin { jvmToolchain(21) }

dependencies {
    // ====================================
    // CORE
    // ====================================
    implementation(files("libs/TwsApi.jar"))
    implementation("com.google.protobuf:protobuf-java:4.31.1")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")

    // ====================================
    // SPRING
    // ====================================

    implementation(kotlin("reflect"))
    implementation("org.springframework.boot:" + "spring-boot-starter-security-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-flyway")

    // Database
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")

    // ====================================
    // TESTS
    // ====================================

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test-junit5"))
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// ====================================
// Trading-core MAIN
// ====================================

application { mainClass.set("MainKt") }

tasks.test { useJUnitPlatform() }

// ====================================
// Spring MAIN
// ====================================

springBoot { mainClass.set("api.SpringMainKt")}
