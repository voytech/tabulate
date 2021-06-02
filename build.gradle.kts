val kotlinVersion: String by project
val guavaVersion: String by project
val reactiveStreamsVersion: String by project
val junitVersion: String by project

plugins {
    java
    kotlin("jvm")
    id("pl.allegro.tech.build.axion-release")
    id("maven-publish")
    id("io.github.gradle-nexus.publish-plugin") version("1.0.0")
    id("signing")
}

scmVersion {
    tag.prefix = "tabulate"
    tag.versionSeparator = "-"
}

allprojects {
    group = "io.github.voytech"
    repositories {
        mavenCentral()
        jcenter()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    dependencies {
        implementation(kotlin("stdlib", kotlinVersion))
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

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenKotlin") {
            from(components["java"])
            artifacts {
                artifact(tasks["javadocJar"])
                artifact(tasks["sourcesJar"])
            }
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

signing {
    useInMemoryPgpKeys(
        System.getenv("GPG_KEY_ID"),
        System.getenv("GPG_PRIVATE_KEY"),
        System.getenv("GPG_PRIVATE_KEY_PASSWORD")
    )
    sign(publishing.publications)
}
