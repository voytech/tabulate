val kotlinVersion: String by project
val apachePoiVersion: String by project
val commonsMathVersion: String by project

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":tabulate-core"))
    implementation("org.apache.poi:poi:$apachePoiVersion")
    implementation("org.apache.poi:poi-ooxml:$apachePoiVersion")
    implementation("org.apache.commons:commons-math3:$commonsMathVersion")
    testImplementation("org.junit.jupiter:junit-jupiter")
}
