plugins {
    kotlin("jvm")
    application
}

group = "app.trading"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("com.google.code.gson:gson:2.13.2")

    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0"
    )
    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.11.0"
    )

    testImplementation(kotlin("test-junit5"))
    testImplementation(
        platform("org.junit:junit-bom:5.11.4")
    )

    testImplementation(
        "org.junit.jupiter:junit-jupiter"
    )

    testRuntimeOnly(
        "org.junit.platform:junit-platform-launcher"
    )
}

application {
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
}