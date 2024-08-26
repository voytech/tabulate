val kotlinVersion: String by project
val junitVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("pl.allegro.tech.build.axion-release")
}

scmVersion {
    tag.prefix = "tabulate.test"
    tag.versionSeparator = "-"
}

dependencies {
    implementation("org.jetbrains.kotlin","kotlin-test", kotlinVersion)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    implementation(project(":tabulate-core"))
}