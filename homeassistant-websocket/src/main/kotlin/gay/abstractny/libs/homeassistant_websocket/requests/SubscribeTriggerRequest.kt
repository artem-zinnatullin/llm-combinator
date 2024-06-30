package gay.abstractny.libs.homeassistant_websocket.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscribeTriggerRequest(
    @SerialName("id")
    val id: Long,

    @SerialName("type")
    private val type: String = "subscribe_trigger",

    @SerialName("trigger")
    private val trigger: Trigger,
)
