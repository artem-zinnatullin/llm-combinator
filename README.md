# llm-combinator

LLM Combinator exports real-time results of multi-modal Ollama executions with input from Frigate cameras and HA sensors into HomeAssistant, recursively.

# Milestones

- [x] Ollama prompts with multi-modal (images) models
- [x] Frigate latest.jpg over HTTP
- [x] Frigate reactive motion updates over MQTT
- [x] Real-time export of Ollama response fields as HomeAssistant binary sensors
- [x] Extensive YAML configuration file
- [] Docker image published to GitHub Container Registry
- [] Authentication with all network components
- [] Recursive inclusion of HomeAssistant sensors into model prompt
- [] LLM-based automations
- [] Extensive logging configuration

# Local Development

Project is built in Kotlin JVM with Gradle Build System and is fully supported by free version of IntelliJ IDEA.
