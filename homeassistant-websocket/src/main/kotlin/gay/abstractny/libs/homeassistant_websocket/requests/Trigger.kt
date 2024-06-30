package gay.abstractny.libs.homeassistant_websocket.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Trigger(
    @SerialName("platform")
    val platform: String,

    @SerialName("entity_id")
    val entityId: String,

    // "from" and "to" are omitted as they're not needed for llm-combinator.
)
