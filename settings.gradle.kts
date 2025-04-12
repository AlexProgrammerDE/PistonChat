enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "PistonChatSuite"

include(
    "PistonChat",
    "PistonMute",
    "PistonFilter"
)
