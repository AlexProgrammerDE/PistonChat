plugins {
    id("pc.shadow-conventions")
    alias(libs.plugins.runpaper)
}

description = "Mute addon for PistonChat."

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly(projects.pistonChat)

    implementation("net.pistonmaster:PistonUtils:1.4.0")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("de.exlll:configlib-paper:4.8.1")
}

tasks {
    runServer {
        minecraftVersion(libs.versions.runpaperversion.get())
    }
}
