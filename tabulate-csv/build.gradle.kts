plugins {
    java
    kotlin("jvm")
    id("pl.allegro.tech.build.axion-release")
}

scmVersion {
    tag.prefix = "tabulate.csv"
    tag.versionSeparator = "-"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":tabulate-core"))
    testImplementation(project(":tabulate-test"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
