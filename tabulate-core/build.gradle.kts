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

sourceSets.create("jmh") {
    java.setSrcDirs(listOf("src/jmh/kotlin"))
}

dependencies {
    "jmhImplementation"(project)
    "jmhImplementation"("org.openjdk.jmh:jmh-core:1.21")
    "kaptJmh"("org.openjdk.jmh:jmh-generator-annprocess:1.21")
}
