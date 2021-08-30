val kotlinVersion: String by project
val projectReactorVersion: String by project

plugins {
    kotlin("jvm")
    id("pl.allegro.tech.build.axion-release")
}

scmVersion {
    tag.prefix = "tabulate.reactor"
    tag.versionSeparator = "-"
}


dependencies {
    implementation(project(":tabulate-core"))
    implementation("io.projectreactor","reactor-core", projectReactorVersion)
    testImplementation("io.projectreactor","reactor-test", projectReactorVersion)
    testImplementation("org.junit.jupiter:junit-jupiter")
}
