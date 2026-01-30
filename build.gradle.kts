plugins {
    base
}

tasks.named<UpdateDaemonJvm>("updateDaemonJvm") {
    languageVersion = JavaLanguageVersion.of(25)
}

allprojects {
    version = property("maven_version")!!
    group = "net.pistonmaster"

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
            name = "spigot-snapshots"
        }
        maven("https://central.sonatype.com/repository/maven-snapshots/") {
            name = "sonatype"
        }
        maven("https://repo.codemc.org/repository/maven-public") {
            name = "codemc"
        }
        maven("https://jitpack.io") {
            name = "jitpack"
        }
        maven("https://repo.tcoded.com/releases") {
            name = "tcoded-releases"
            content {
                includeGroup("com.tcoded")
            }
        }
    }
}
