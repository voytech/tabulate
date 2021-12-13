pluginManagement {
    val kotlinVersion: String by settings
    val axionReleasePlugin: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        id("pl.allegro.tech.build.axion-release") version axionReleasePlugin
    }
}

rootProject.name = "tabulate"
include("tabulate-core", "tabulate-test", "tabulate-excel", "tabulate-reactor", "tabulate-bom")