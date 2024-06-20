package gay.abstractny.libs.frigate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FrigateConfig(
    @SerialName("cameras") val cameras: Map<String, JsonElement>,
    @SerialName("mqtt") val mqtt: Mqtt,
) {
    @Serializable
    data class Mqtt(
        @SerialName("topic_prefix") val topicPrefix: String
    )
}
