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