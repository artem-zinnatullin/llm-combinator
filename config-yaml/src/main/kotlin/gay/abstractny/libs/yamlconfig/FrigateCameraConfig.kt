package gay.abstractny.libs.yamlconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FrigateCameraConfig(
    @SerialName("name")
    val name: String,

    @SerialName("llm_prompts")
    val llmPrompts: List<FrigateCameraLLMPromptConfig>,

    @SerialName("periodic_update_sec")
    val periodicUpdateSec: Int,
)
