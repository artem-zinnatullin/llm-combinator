plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(libs.rxjava)
    api(project(":config-yaml"))
    api(project(":frigate-http"))
    api(project(":frigate-mqtt"))
    api(project(":ollama-http"))
}

dependencies {
    implementation(libs.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(kotlin("reflect"))
}
