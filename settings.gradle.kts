rootProject.name = "llm-combinator"

buildCache {
    local {
        isEnabled = true
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":libs:frigate")
include(":libs:frigate-mqtt")
include(":libs:homeassistant-http")
include(":libs:homeassistant-mqtt")
include(":libs:llm-cameras")
include(":libs:ollama")
include(":libs:yaml-config")
include(":llm-combinator")
