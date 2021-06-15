val kotlinVersion: String by project
val apachePoiVersion: String by project
val commonsMathVersion: String by project
val projectReactorVersion: String by project

plugins {
    kotlin("jvm")
    id("pl.allegro.tech.build.axion-release")
}

scmVersion {
    tag.prefix = "tabulate.excel"
    tag.versionSeparator = "-"
}

dependencies {
    implementation(project(":tabulate-core"))
    api("org.apache.poi:poi:$apachePoiVersion")
    api("org.apache.poi:poi-ooxml:$apachePoiVersion")
    implementation("org.apache.commons:commons-math3:$commonsMathVersion")
    testImplementation("io.projectreactor","reactor-core", projectReactorVersion)
    testImplementation("io.projectreactor","reactor-test", projectReactorVersion)
    testImplementation("org.junit.jupiter:junit-jupiter")
}
