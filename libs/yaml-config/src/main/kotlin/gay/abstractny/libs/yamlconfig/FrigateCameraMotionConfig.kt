package gay.abstractny.libs.yamlconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FrigateCameraMotionConfig(
    @SerialName("enabled")
    val enabled: Boolean,
)
