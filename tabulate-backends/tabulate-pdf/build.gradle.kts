val pdfBoxVersion: String by project

plugins {
    kotlin("jvm")
    id("pl.allegro.tech.build.axion-release")
}

scmVersion {
    tag.prefix = "tabulate.pdf"
    tag.versionSeparator = "-"
}

dependencies {
    implementation(project(":tabulate-core"))
    implementation("org.apache.pdfbox:pdfbox:$pdfBoxVersion")
    testImplementation(project(":tabulate-test"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}