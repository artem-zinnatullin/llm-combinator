plugins {
    id("org.jetbrains.kotlin.jvm") version libs.versions.kotlin
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlin
    id("io.gitlab.arturbosch.detekt") version(libs.versions.detekt)
}


subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        config.setFrom(rootProject.file("gradle/detekt.yaml"))
    }
}
