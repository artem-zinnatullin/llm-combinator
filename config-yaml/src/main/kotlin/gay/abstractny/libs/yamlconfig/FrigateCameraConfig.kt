package gay.abstractny.libs.yamlconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FrigateCameraConfig(
    @SerialName("name")
    val name: String,

    @SerialName("periodic_update_sec")
    val periodicUpdateSec: Int,

    @SerialName("motion_updates")
    val motionUpdates: FrigateCameraMotionConfig = FrigateCameraMotionConfig(
        enabled = true,
    ),
)
