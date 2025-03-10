plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        url = uri("https://repo.codemc.org/repository/maven-public")
    }
    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

dependencies {
    api(libs.org.bstats.bstats.bukkit)
    api(libs.net.pistonmaster.pistonutils)
    api(libs.org.mariadb.jdbc.mariadb.java.client)
    api(libs.net.kyori.adventure.text.minimessage)
    api(libs.net.kyori.adventure.platform.bukkit)
    compileOnly(libs.org.spigotmc.spigot.api)
    compileOnly(libs.com.google.code.findbugs.jsr305)
    compileOnly(libs.io.github.miniplaceholders.miniplaceholders.api)
    compileOnly(libs.org.projectlombok.lombok)
    annotationProcessor(libs.org.projectlombok.lombok)
}

group = "net.pistonmaster"
version = "1.6.0"
description = "An advanced chat plugin for survival/anarchy servers."

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}
