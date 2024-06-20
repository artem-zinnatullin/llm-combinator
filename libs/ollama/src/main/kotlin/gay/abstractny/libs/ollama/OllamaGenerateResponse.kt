package gay.abstractny.libs.ollama

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OllamaGenerateResponse(
    @SerialName("model") val model: String,
    @SerialName("response") val response: String,
    @SerialName("total_duration") val totalDurationMicroseconds: Long,
)
