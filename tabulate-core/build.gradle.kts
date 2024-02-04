plugins {
    kotlin("jvm")
    id("pl.allegro.tech.build.axion-release")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.9.0"
}

scmVersion {
    tag.prefix = "tabulate.core"
    tag.versionSeparator = "-"
}

dependencies {
    implementation("io.github.microutils:kotlin-logging")
}
