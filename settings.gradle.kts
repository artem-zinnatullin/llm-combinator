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

include(":config-yaml")
include(":frigate-http")
include(":frigate-mqtt")
include(":homeassistant-http")
include(":homeassistant-mqtt")
include(":llm-cameras")
include(":llm-combinator")
include(":ollama-http")
