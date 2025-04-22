plugins {
    id("pc.shadow-conventions")
    alias(libs.plugins.runpaper)
}

description = "Chat filter addon for PistonChat."

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly(projects.pistonChat)
    compileOnly(projects.pistonMute)

    implementation("net.pistonmaster:PistonUtils:1.4.0")
    implementation("me.xdrop:fuzzywuzzy:1.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.12.2")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("org.apache.commons:commons-collections4:4.5.0")
}

tasks {
    runServer {
        minecraftVersion(libs.versions.runpaperversion.get())
    }
}
