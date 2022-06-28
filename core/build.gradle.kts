plugins {
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1" // Generates plugin.yml
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")

    implementation(project(":nms"))
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