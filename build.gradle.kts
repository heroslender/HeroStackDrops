import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.3.3"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1" // Generates plugin.yml
}

group = "com.heroslender"
version = "1.10.0"

repositories {
    mavenCentral()

    maven("https://nexus.heroslender.com/repository/maven-public/")
    maven("https://repo.codemc.io/repository/nms/")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    compileOnly("org.jetbrains:annotations:22.0.0")
}

java {
    withJavadocJar()
    withSourcesJar()

    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks {
    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}

// Configure plugin.yml generation
bukkit {
    main = "com.heroslender.herostackdrops.StackDrops"
    apiVersion = "1.18"
    authors = listOf("Heroslender")
    version = project.version.toString()

    name = "StackDrops"
    description = "Plugin de juntar os itens dropados"
    website = "https://www.heroslender.com/"
}