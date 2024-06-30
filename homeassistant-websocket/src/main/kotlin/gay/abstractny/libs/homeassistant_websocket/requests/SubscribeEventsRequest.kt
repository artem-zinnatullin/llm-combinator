package gay.abstractny.libs.homeassistant_websocket.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscribeEventsRequest(
    @SerialName("id")
    val id: Long,

    @SerialName("type")
    private val type: String = "subscribe_events",

    @SerialName("event_type")
    private val eventType: EventType?,
)
