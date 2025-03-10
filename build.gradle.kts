plugins {
    base
}

allprojects {
    group = "net.pistonmaster"
    version = "1.6.0"
    description = "An advanced chat plugin for survival/anarchy servers."

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
            name = "spigot-snapshots"
        }
        maven("https://oss.sonatype.org/content/groups/public/") {
            name = "sonatype"
        }
        maven("https://repo.codemc.org/repository/maven-public") {
            name = "codemc"
        }
    }
}

tasks.create("outputVersion") {
    doLast {
        println(project.version)
    }
}
