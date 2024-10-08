val kotlinVersion: String by project
val guavaVersion: String by project
val junitVersion: String by project
val kotlinLoggingVersion: String by project

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.7.0" apply false

    id("java-library")
    id("org.jetbrains.dokka") version "1.4.32"
    id("pl.allegro.tech.build.axion-release")
    id("maven-publish")
    id("io.github.gradle-nexus.publish-plugin") version("1.0.0")
    id("signing")
    //id("jacoco")
}

scmVersion {
    tag.prefix = "tabulate"
    tag.versionSeparator = "-"
}

version = scmVersion.version

allprojects {
    group = "io.github.voytech"
    version = rootProject.version
    repositories {
        mavenCentral()
        jcenter()
    }
}

configure(
    listOf(
        project(":tabulate-core"),
        project(":tabulate-test"),
        project(":tabulate-backends:tabulate-excel"),
        project(":tabulate-backends:tabulate-csv"),
        project(":tabulate-backends:tabulate-pdf"),
        project(":tabulate-backends:tabulate-backends-tests"),
    )
) {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.dokka")
    //apply(plugin = "jacoco")
    apply(plugin = "signing")
    dependencies {
        implementation(kotlin("stdlib", kotlinVersion))
        implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
        implementation("ch.qos.logback:logback-classic:1.2.6")
        testImplementation("org.jetbrains.kotlin","kotlin-test", kotlinVersion)
        testImplementation(platform("org.junit:junit-bom:$junitVersion"))
        testImplementation("org.junit.jupiter","junit-jupiter")
        testRuntimeOnly("org.jetbrains.kotlin","kotlin-reflect", kotlinVersion)
    }
    //jacoco {
    //    toolVersion = "0.8.7"
    //}
    configurations.all {
        resolutionStrategy {
            eachDependency {
                //if ("org.jacoco" == this.requested.group) {
                //    useVersion("0.8.7")
                //}
            }
        }
    }
    tasks {
        test {
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
            }
            //finalizedBy(jacocoTestReport) // report is always generated after tests run
        }

        register<Jar>("dokkaJavadocJar") {
            from("$buildDir/dokka/javadoc")
            dependsOn("dokkaJavadoc")
            archiveClassifier.set("javadoc")
        }
/*
        jacocoTestReport {
            dependsOn(test) // tests are required to run before generating the report
            reports {
                xml.isEnabled = true
                xml.destination = file("${buildDir}/reports/jacoco/report.xml")
                html.isEnabled = true
            }
        }
        */
    }

    java {
        withSourcesJar()
    }

    publishing {
        publications {
            create<MavenPublication>("tabulate") {
                from(components["java"])
                artifact(tasks["dokkaJavadocJar"])
                pom {
                    name.set(project.name)
                    description.set("Kotlin/JVM library to simplify exporting collections of objects into tabular file formats")
                    url.set("https://github.com/voytech/tabulate")
                    inceptionYear.set("2021")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("voytech")
                            name.set("Wojciech Mąka")
                        }
                    }
                    scm {
                        url.set("https://github.com/voytech/tabulate")
                        connection.set("scm:git@github.com:voytech/tabulate.git")
                        developerConnection.set("scm:git@github.com:voytech/tabulate.git")
                    }
                }
            }
        }
    }

    if (System.getenv("ENABLE_GPG") != null) {
        signing {
            useGpgCmd()
            sign(publishing.publications)
        }
    }

}

nexusPublishing {
    repositories {
        sonatype {
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}
