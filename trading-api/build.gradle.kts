plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
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
    implementation(project(":trading-core"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}