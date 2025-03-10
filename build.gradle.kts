plugins {
    base
}

allprojects {
    group = "net.pistonmaster"

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

tasks.register("outputVersion") {
    doLast {
        println(project.version)
    }
}
