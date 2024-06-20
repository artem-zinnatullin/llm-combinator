package gay.abstractny.libs.llmcameras.responses

import gay.abstractny.libs.llmcameras.DESCRIPTION_PROMPT
import gay.abstractny.libs.llmcameras.EMERGENCY_PROMPT
import gay.abstractny.libs.llmcameras.LLMPrompt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TestCameraLLMResponse(
    @SerialName("human_uses_desk")
    @LLMPrompt("If there is a human using the desk — set true")
    val humanUsesDesk: Boolean,

    @SerialName("emergency")
    @LLMPrompt(EMERGENCY_PROMPT)
    val emergency: Boolean,

    @SerialName("description")
    @LLMPrompt(DESCRIPTION_PROMPT)
    val description: String,
)
