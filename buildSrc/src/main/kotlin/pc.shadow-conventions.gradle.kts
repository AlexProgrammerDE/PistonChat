import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("pc.java-conventions")
    id("com.gradleup.shadow")
}

tasks {
    jar {
        archiveClassifier.set("unshaded")
        from(project.rootProject.file("LICENSE"))
    }

    shadowJar {
        archiveClassifier.set("")
        exclude("META-INF/SPONGEPO.SF", "META-INF/SPONGEPO.DSA", "META-INF/SPONGEPO.RSA")
        minimize()
        configureRelocations()
    }

    build {
        dependsOn(shadowJar)
    }
}

fun ShadowJar.configureRelocations() {
    relocate("org.bstats", "net.pistonmaster.pistonchat.shadow.bstats")
    relocate("net.pistonmaster.pistonutils", "net.pistonmaster.pistonchat.shadow.pistonutils")
    relocate("org.intellij.lang.annotations", "net.pistonmaster.pistonchat.shadow.annotations.intellij")
    relocate("org.jetbrains.annotations", "net.pistonmaster.pistonchat.shadow.annotations.jetbrains")
    relocate("com.google.gson", "net.pistonmaster.pistonchat.shadow.gson")
    relocate("com.google.errorprone", "net.pistonmaster.pistonchat.shadow.errorprone")
    relocate("com.github.benmanes.caffeine", "net.pistonmaster.pistonchat.shadow.caffeine")
    relocate("net.kyori", "net.pistonmaster.pistonchat.shadow.kyori")
}
