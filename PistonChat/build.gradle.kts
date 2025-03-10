plugins {
    id("pc.shadow-conventions")
}

dependencies {
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.2.3")
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")

    implementation("net.kyori:adventure-platform-bukkit:4.3.4")
    implementation("net.kyori:adventure-text-minimessage:4.18.0")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.1")
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")
}
