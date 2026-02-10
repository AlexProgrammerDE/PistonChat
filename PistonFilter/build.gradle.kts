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
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:6.0.2")
    testImplementation("org.mockito:mockito-core:5.21.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.21.0")
    testImplementation("me.xdrop:fuzzywuzzy:1.4.0")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("org.apache.commons:commons-collections4:4.5.0")
    implementation("de.exlll:configlib-paper:4.8.1")
}

tasks {
    runServer {
        minecraftVersion(libs.versions.runpaperversion.get())
    }
}
