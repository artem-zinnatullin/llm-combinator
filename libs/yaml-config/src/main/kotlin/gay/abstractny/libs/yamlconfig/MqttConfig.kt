package gay.abstractny.libs.yamlconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MqttConfig(
    @SerialName("host")
    val host: String,

    @SerialName("port")
    val port: Int,
)
