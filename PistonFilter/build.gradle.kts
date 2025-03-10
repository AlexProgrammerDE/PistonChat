plugins {
    id("pc.shadow-conventions")
}

version = "1.3.0"
description = "Chat filter addon for PistonChat."

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly(projects.pistonChat)
    compileOnly(projects.pistonMute)
    implementation("me.xdrop:fuzzywuzzy:1.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.12.0")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("net.pistonmaster:PistonUtils:1.3.2")
    implementation("org.apache.commons:commons-collections4:4.4")
}
