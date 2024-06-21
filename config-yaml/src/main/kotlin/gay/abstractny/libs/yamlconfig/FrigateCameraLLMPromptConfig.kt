package gay.abstractny.libs.yamlconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FrigateCameraLLMPromptConfig(

    /**
     * Defaults to [FrigateConfig.defaultLLMModel], otherwise prefers the value set here.
     */
    @SerialName("model")
    val model: String = "",

    @SerialName("prefix")
    val prefix: String = "You are image analyzer that replies ONLY in VALID JSON with following schema: {",

    @SerialName("postfix")
    val postfix: String = "}. For each JSON property use its name from schema and compute its value as requested type.",

    @SerialName("properties")
    val properties: List<FrigateCameraLLMPromptPropertyConfig>,

    @SerialName("periodic_update_sec")
    val periodicUpdateSec: Int = -1,

    @SerialName("motion_updates")
    val motionUpdates: FrigateCameraMotionConfig = FrigateCameraMotionConfig(
        enabled = true,
    ),
)
