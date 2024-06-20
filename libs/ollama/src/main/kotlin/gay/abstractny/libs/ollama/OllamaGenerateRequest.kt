package gay.abstractny.libs.ollama

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OllamaGenerateRequest(
    val model: String,
    val prompt: String,
    @SerialName("images") val imagesBase64: List<String>? = null,
    @SerialName("format")
    val format: String? = null,
    private val stream: Boolean = false,
    val options: Options? = Options(temperature = 0.0f)
) {
    @Serializable
    data class Options(
        val temperature: Float,
    )
}
