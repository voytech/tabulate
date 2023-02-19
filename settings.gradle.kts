pluginManagement {
    val kotlinVersion: String by settings
    val axionReleasePlugin: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        id("pl.allegro.tech.build.axion-release") version axionReleasePlugin
    }
}

rootProject.name = "tabulate"
include("tabulate-bom")
include("tabulate-core")
include(":tabulate-test")
// Project component backends. Those projects provide rendering backend implementations for different DocumentFormats.
include("tabulate-backends")
include("tabulate-backends:tabulate-excel")
include("tabulate-backends:tabulate-pdf")
include("tabulate-backends:tabulate-csv")
include("tabulate-backends:tabulate-pdf")