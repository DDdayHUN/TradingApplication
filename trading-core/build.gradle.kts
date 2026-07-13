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

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
}

application {
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
}