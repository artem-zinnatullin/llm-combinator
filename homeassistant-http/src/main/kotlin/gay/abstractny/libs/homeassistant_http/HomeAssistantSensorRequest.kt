package gay.abstractny.libs.homeassistant_http

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * See https://www.home-assistant.io/integrations/http/#sensor
 */
@Serializable
data class HomeAssistantSensorRequest(
    @SerialName("state") val state: String,
    @SerialName("attributes") val attributes: Attributes,
) {
    @Serializable
    data class Attributes(
        @SerialName("friendly_name") val friendlyName: String,
        @SerialName("unit_of_measurement") val unitOfMeasurement: String,
    )
}
