plugins {
    base
    id("org.openrewrite.rewrite") version "latest.release"
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

dependencies {
    rewrite(platform("org.openrewrite.recipe:rewrite-recipe-bom:latest.release"))
    rewrite("org.openrewrite.recipe:rewrite-java")
}

rewrite {
    activeRecipe("org.openrewrite.java.ShortenFullyQualifiedTypeReferences")
}

tasks.register("outputVersion") {
    doLast {
        println(project.version)
    }
}
