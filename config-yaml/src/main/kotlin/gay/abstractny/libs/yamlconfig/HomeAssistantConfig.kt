package gay.abstractny.libs.yamlconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeAssistantConfig(
    @SerialName("url")
    val url: String,

    @SerialName("update_sensors")
    val updateSensors: Boolean = true,
)
