val apachePoiVersion: String by project
val commonsMathVersion: String by project
val reactiveStreamsVersion: String by project

plugins {
    id("java-platform")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    constraints {
        api(project(":tabulate-core"))
        api(project(":tabulate-excel"))
        api("org.apache.poi:poi:$apachePoiVersion") // # kts bug when form is: api("org.apache.poi","poi",apachePoiVersion)
        api("org.apache.poi:poi-ooxml:$apachePoiVersion")
        api("org.apache.commons:commons-math3:$commonsMathVersion")
        api("org.reactivestreams:reactive-streams:$reactiveStreamsVersion")
    }
}

publishing {
    publications {
        create<MavenPublication>("tabulate-bom") {
            from(components["javaPlatform"])
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