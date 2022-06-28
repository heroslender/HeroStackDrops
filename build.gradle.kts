plugins {
    `java`
    `java-library`
}

group = "com.heroslender"
version = "1.11.0"

allprojects {
    apply {
        plugin("org.gradle.java")
    }

    repositories {
        mavenCentral()

        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/sonatype-nexus-snapshots/")
        maven("https://nexus.heroslender.com/repository/maven-public/")
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.22")
        annotationProcessor("org.projectlombok:lombok:1.18.22")

        compileOnly("org.jetbrains:annotations:22.0.0")
    }

    java {
        withJavadocJar()
        withSourcesJar()

        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    tasks {
        compileJava {
            options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        }

        javadoc {
            options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        }

        processResources {
            filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
        }
    }
}