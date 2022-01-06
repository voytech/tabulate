val kotlinVersion: String by project
val junitVersion: String by project

plugins {
    kotlin("jvm")
    id("pl.allegro.tech.build.axion-release")
}

scmVersion {
    tag.prefix = "tabulate.test"
    tag.versionSeparator = "-"
}

dependencies {
    implementation("org.jetbrains.kotlin","kotlin-test", kotlinVersion)
    implementation(project(":tabulate-core"))
}