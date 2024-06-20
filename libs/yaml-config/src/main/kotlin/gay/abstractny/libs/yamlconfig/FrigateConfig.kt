package gay.abstractny.libs.yamlconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FrigateConfig(
    @SerialName("servers")
    val servers: List<FrigateServerConfig>,

    @SerialName("cameras")
    val cameras: List<FrigateCameraConfig>,
)
