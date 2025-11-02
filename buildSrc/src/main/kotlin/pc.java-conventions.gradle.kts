plugins {
    `java-library`
    `maven-publish`
    id("net.ltgt.errorprone")
    id("com.github.spotbugs")
}

spotbugs {
    ignoreFailures = true
}

dependencies {
    compileOnlyApi("org.apiguardian:apiguardian-api:1.1.2")

    errorprone("com.google.errorprone:error_prone_core:2.43.0")
    spotbugs("com.github.spotbugs:spotbugs:4.9.8")

    compileOnly("net.luckperms:api:5.5")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.1")
    testImplementation("org.mockito:mockito-core:5.20.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.20.0")
}

tasks {
    processResources {
        expand(
            mapOf(
                "version" to version,
                "description" to description,
                "url" to "https://pistonmaster.net/PistonChat"
            )
        )
    }
    test {
        reports.junitXml.required = true
        reports.html.required = true
        useJUnitPlatform()
        maxParallelForks = Runtime.getRuntime().availableProcessors().div(2).coerceAtLeast(1)
    }
    jar {
        from(rootProject.file("LICENSE"))
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:all,-serial,-processing")
}

tasks.withType<Javadoc> {
    enabled = false
}

val repoName = if (version.toString().endsWith("SNAPSHOT")) "maven-snapshots" else "maven-releases"
publishing {
    repositories {
        maven("https://repo.codemc.org/repository/${repoName}/") {
            credentials.username = System.getenv("CODEMC_USERNAME")
            credentials.password = System.getenv("CODEMC_PASSWORD")
            name = "codemc"
        }
    }
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name = "PistonChat"
                description = rootProject.description
                url = "https://github.com/AlexProgrammerDE/PistonChat"
                organization {
                    name = "AlexProgrammerDE"
                    url = "https://pistonmaster.net"
                }
                developers {
                    developer {
                        id = "AlexProgrammerDE"
                        timezone = "Europe/Berlin"
                        url = "https://pistonmaster.net"
                    }
                }
                licenses {
                    license {
                        name = "GNU General Public License v3.0"
                        url = "https://www.gnu.org/licenses/gpl-3.0.html"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/AlexProgrammerDE/PistonChat.git"
                    developerConnection = "scm:git:ssh://git@github.com/AlexProgrammerDE/PistonChat.git"
                    url = "https://github.com/AlexProgrammerDE/PistonChat"
                }
                ciManagement {
                    system = "GitHub Actions"
                    url = "https://github.com/AlexProgrammerDE/PistonChat/actions"
                }
                issueManagement {
                    system = "GitHub"
                    url = "https://github.com/AlexProgrammerDE/PistonChat/issues"
                }
            }
        }
    }
}
