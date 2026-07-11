plugins {
    kotlin("jvm") version "2.3.0"
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "app.trading"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependecies {
    // GSON
    implementation("com.google.code.gson:gson:2.13.2")

    // KOTLIN
    implementation("org.jetbrains.kotlin:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlin:kotlinx-coroutines-jdk8:1.11.0")

    // JMETRO
    implementation("org.jfxtras:jmetro:11.6.16"){
        exclude(group = "org.openjfx", module = "javafx-base")
        exclude(group = "org.openjfx", module = "javafx-graphics")
        excluede(group = "org.openjfx", module = "javafx-controls")
    }

    // TEST
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
}

javafx {
    version = "21.0.8"
    modules("javafx.controls")
}

application {
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
}