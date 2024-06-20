package gay.abstractny.libs.homeassistant_mqtt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * See https://www.home-assistant.io/integrations/mqtt#discovery-examples
 */
@Serializable
data class HomeAssistantMqttDeviceConfiguration(
    @SerialName("name") val name: String?,
    @SerialName("device_class") val deviceClass: String,
    @SerialName("state_topic") val stateTopic: String,
    @SerialName("uniqueId") val uniquiId: String,
    @SerialName("device") val device: HomeAssistantMqttDevice,
)
