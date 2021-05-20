val kotlinVersion: String by project
val guavaVersion: String by project
val reactiveStreamsVersion: String by project
val junitVersion: String by project

plugins {
    kotlin("jvm")
}

dependencies {
    implementation("org.jetbrains.kotlin","kotlin-test", kotlinVersion)
    testImplementation("io.projectreactor:reactor-core:3.4.5")
    testImplementation("io.projectreactor:reactor-test:3.4.5")
}