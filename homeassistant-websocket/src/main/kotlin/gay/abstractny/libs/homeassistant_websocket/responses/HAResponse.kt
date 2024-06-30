package gay.abstractny.libs.homeassistant_websocket.responses

import kotlinx.serialization.json.JsonObject

data class HAResponse(
    val jsonObject: JsonObject,
)
