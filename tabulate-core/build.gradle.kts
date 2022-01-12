plugins {
    kotlin("jvm")
    id("pl.allegro.tech.build.axion-release")
    id("org.jetbrains.kotlin.kapt")
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
