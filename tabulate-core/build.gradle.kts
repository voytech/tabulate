val kotlinVersion: String by project
val guavaVersion: String by project
val reactiveStreamsVersion: String by project
val junitVersion: String by project
val projectReactorVersion: String by project

plugins {
    kotlin("jvm")
    id("pl.allegro.tech.build.axion-release")
}

scmVersion {
    tag.prefix = "tabulate.core"
    tag.versionSeparator = "-"
}

dependencies {
    implementation("org.jetbrains.kotlin","kotlin-test", kotlinVersion)
    api("org.reactivestreams","reactive-streams", reactiveStreamsVersion)
    testImplementation("io.projectreactor","reactor-core", projectReactorVersion)
    testImplementation("io.projectreactor","reactor-test", projectReactorVersion)
}