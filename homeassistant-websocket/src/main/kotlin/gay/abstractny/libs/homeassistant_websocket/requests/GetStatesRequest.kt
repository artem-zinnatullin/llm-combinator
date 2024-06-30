package gay.abstractny.libs.homeassistant_websocket.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetStatesRequest(
    @SerialName("id")
    val id: Long,

    @SerialName("type")
    private val type: String = "get_states",
)
