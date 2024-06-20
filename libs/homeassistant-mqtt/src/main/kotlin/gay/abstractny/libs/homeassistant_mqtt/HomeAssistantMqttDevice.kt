package gay.abstractny.libs.homeassistant_mqtt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * See https://www.home-assistant.io/integrations/mqtt#discovery-examples
 */
@Serializable
data class HomeAssistantMqttDevice(
    @SerialName("identifiers") val identifiers: List<String>,
    @SerialName("name") val name: String,
)
