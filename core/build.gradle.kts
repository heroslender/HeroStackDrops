plugins {
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1" // Generates plugin.yml
    id("com.github.johnrengelman.shadow") version "5.2.0"
    `maven-publish`
    `java-library`
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")

    api(project(":nms"))
    implementation(project(":nms:v1_8_R3"))
    implementation(project(":nms:v1_18_R2"))
}

// Configure plugin.yml generation
bukkit {
    main = "com.heroslender.herostackdrops.StackDrops"
    apiVersion = "1.18"
    authors = listOf("Heroslender")
    version = parent?.version.toString()

    name = "StackDrops"
    description = "Plugin de juntar os itens dropados"
    website = "https://www.heroslender.com/"

    commands {
        create("herostackdrops") {
            aliases = listOf("stackdrops")
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            val target = if (project.version.toString().endsWith("-SNAPSHOT"))
                "https://nexus.heroslender.com/repository/maven-snapshots/"
            else
                "https://nexus.heroslender.com/repository/maven-releases/"

            name = "heroslender-nexus"
            url = uri(target)

            credentials {
                username = project.findProperty("nexus.user") as? String
                    ?: System.getenv("NEXUS_USERNAME")
                password = project.findProperty("nexus.key") as? String
                    ?: System.getenv("NEXUS_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            artifactId = "StackDrops"
            groupId = project.group.toString()
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://github.com/heroslender/HeroStackDrops")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/heroslender/HeroStackDrops/blob/main/LICENSE")
                    }
                }

                developers {
                    developer {
                        id.set("heroslender")
                        name.set("Bruno Martins")
                        email.set("admin@heroslender.com")
                    }
                }

                scm {
                    connection.set("https://github.com/heroslender/HeroStackDrops.git")
                    developerConnection.set("git@github.com:heroslender/HeroStackDrops.git")
                    url.set("https://github.com/heroslender/HeroStackDrops")
                }
            }
        }
    }
}