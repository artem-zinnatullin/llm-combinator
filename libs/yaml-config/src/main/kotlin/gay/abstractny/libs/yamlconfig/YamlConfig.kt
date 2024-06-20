package gay.abstractny.libs.yamlconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YamlConfig(
    @SerialName("ollama")
    val ollama: OllamaConfig,

    @SerialName("home_assistant")
    val homeAssistant: HomeAssistantConfig,

    @SerialName("frigate")
    val frigate: FrigateConfig,

    // TODO: Make mqtt optional for people without Frigate.
    @SerialName("mqtt")
    val mqtt: MqttConfig,
)
