plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(libs.rxjava)
    api(project(":libs:frigate"))
}

dependencies {
    implementation(libs.rxjavabridge)
    implementation(libs.hivemq.mqtt.client)
    implementation(libs.kotlinx.serialization.json)
}
