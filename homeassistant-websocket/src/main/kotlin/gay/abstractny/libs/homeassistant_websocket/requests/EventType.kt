package gay.abstractny.libs.homeassistant_websocket.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class EventType {

    @SerialName("state_changed")
    StateChanged,
}
