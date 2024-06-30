plugins {
    kotlin("jvm")
    id("application")
    // Shadow plugin allows building -all jar with all dependencies for easy distribution.
    id("com.github.johnrengelman.shadow") version libs.versions.shadow
}

dependencies {
    implementation(libs.clikt)
    implementation(libs.okhttp)
    implementation(libs.rxjava)
    implementation(libs.rxkotlin)
    implementation(libs.logging)
    implementation(libs.slf4j)
}

dependencies {
    implementation(project(":config-yaml"))
    implementation(project(":frigate-http"))
    implementation(project(":frigate-mqtt"))
    implementation(project(":homeassistant-http"))
    implementation(project(":homeassistant-websocket"))
    implementation(project(":llm-cameras"))
    implementation(project(":ollama-http"))
}

application {
    mainClass = "gay.abstractny.microservices.llmcombinator.Main"
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
    skip()
}

tasks {
    shadowJar {
        mergeServiceFiles()
    }

    shadowDistZip {
        archiveBaseName = "llm-combinator"
    }
    shadowDistTar { enabled = false }
    distZip { enabled = false }
    distTar { enabled = false }

    withType<Jar>().configureEach {
        manifest {
            attributes(mapOf("Add-Opens" to "java.base/java.lang"))
        }
    }
}

val shadowDist: Configuration by configurations.consumable("shadowDist")
artifacts.add(shadowDist.name, tasks.shadowDistZip)

