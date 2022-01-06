plugins {
    kotlin("jvm")
    id("pl.allegro.tech.build.axion-release")
}

scmVersion {
    tag.prefix = "tabulate.core"
    tag.versionSeparator = "-"
}

dependencies {
}