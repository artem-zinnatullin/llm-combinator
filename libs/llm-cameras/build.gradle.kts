plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(libs.rxjava)
    api(project(":libs:frigate"))
    api(project(":libs:frigate-mqtt"))
    api(project(":libs:ollama"))
}

dependencies {
    implementation(libs.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(kotlin("reflect"))
}
