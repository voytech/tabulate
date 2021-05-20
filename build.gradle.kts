val kotlinVersion: String by project
val guavaVersion: String by project
val reactiveStreamsVersion: String by project
val junitVersion: String by project

plugins {
    java
    kotlin("jvm") version "1.5.0"
    id("pl.allegro.tech.build.axion-release") version "1.11.0"
}

scmVersion {
    tag.prefix = "tabulate"
    //tag.versionSeparator = "versionWithBranch"
}

project.version = scmVersion.version

allprojects {
    group = "io.github.voytech"
    version = rootProject.version
    repositories {
        mavenCentral()
        jcenter()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    dependencies {
        //implementation("org.jetbrains.kotlin","kotlin-stdlib-jdk8", kotlinVersion)
        implementation(kotlin("stdlib"))
        implementation("com.google.guava","guava", guavaVersion)
        api("org.reactivestreams","reactive-streams", reactiveStreamsVersion)
        testImplementation("org.jetbrains.kotlin","kotlin-test", kotlinVersion)
        testImplementation(platform("org.junit:junit-bom:$junitVersion"))
        testImplementation("org.junit.jupiter","junit-jupiter")
        testImplementation("com.google.truth","truth","1.0.1")
        testRuntimeOnly("org.jetbrains.kotlin","kotlin-reflect", kotlinVersion)
    }
    tasks {
        test {
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
            }
        }
    }
}
