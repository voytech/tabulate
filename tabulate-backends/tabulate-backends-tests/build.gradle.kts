plugins {
    kotlin("jvm")
    id("pl.allegro.tech.build.axion-release")
}

scmVersion {
    tag.prefix = "tabulate.backends.tests"
    tag.versionSeparator = "-"
}

dependencies {
    testImplementation(project(":tabulate-backends:tabulate-excel"))
    testImplementation(project(":tabulate-backends:tabulate-pdf"))
    testImplementation(project(":tabulate-core"))
    testImplementation(project(":tabulate-test"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
