dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
}

subprojects {
    repositories {
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.codemc.io/repository/nms/")
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}