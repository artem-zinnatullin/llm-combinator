package gay.abstractny.libs.yamlconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FrigateConfig(
    @SerialName("servers")
    val servers: List<FrigateServerConfig>,

    @SerialName("default_llm_model")
    val defaultLLMModel: String? = null,

    @SerialName("cameras")
    val cameras: List<FrigateCameraConfig>,
)
