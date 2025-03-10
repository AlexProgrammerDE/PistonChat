plugins {
    id("pc.shadow-conventions")
}

version = "1.2.0"
description = "Mute addon for PistonChat."

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly(projects.pistonChat)
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("net.pistonmaster:PistonUtils:1.3.2")
}
