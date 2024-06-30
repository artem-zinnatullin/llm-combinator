package gay.abstractny.libs.homeassistant_websocket.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    @SerialName("type")
    private val type: String = "auth",

    @SerialName("access_token")
    val accessToken: String,
)
