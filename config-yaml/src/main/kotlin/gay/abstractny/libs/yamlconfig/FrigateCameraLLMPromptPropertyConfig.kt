package gay.abstractny.libs.yamlconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FrigateCameraLLMPromptPropertyConfig(
    @SerialName("name")
    val name: String,

    @SerialName("prompt")
    val prompt: String,

    @SerialName("type")
    val type: PropertyType,
) {
    @Serializable
    enum class PropertyType {
        @SerialName("boolean")
        BOOLEAN,

        @SerialName("string")
        STRING,
    }
}
