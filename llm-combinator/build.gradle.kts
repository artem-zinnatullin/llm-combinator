plugins {
    kotlin("jvm")
    id("com.bmuschko.docker-java-application") version libs.versions.dockerPlugin
    id("application")
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

val dockerRegistry = "TODO"
val dockerImage = "TODO"
val dockerImageVersion = "TODO"

docker {
    javaApplication {
        baseImage.set("openjdk:21-slim-bullseye")
        maintainer.set("Artem Zinnatullin")
        ports.set(listOf())
        images.set(setOf("$dockerRegistry/$dockerImage:$dockerImageVersion"))
        jvmArgs.set(listOf("-Xmx512m", "-Xmx896m"))
    }
}

task("dockerBuildxAndPushImage", type = Exec::class) {
    group = "docker"
    dependsOn("dockerCreateDockerfile")
    workingDir("build/docker")
    executable("docker")

    args(listOf(
        "buildx",
        "build",
        "--platform", "linux/amd64,linux/arm64",
        "-t", "$dockerRegistry/$dockerImage:$dockerImageVersion",
        "--push", "."
    ))
}
