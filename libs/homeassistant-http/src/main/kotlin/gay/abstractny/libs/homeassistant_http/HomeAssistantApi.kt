package gay.abstractny.libs.homeassistant_http

import io.reactivex.rxjava3.core.Completable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * See https://github.com/ollama/ollama/blob/main/docs/api.md
 */
internal interface HomeAssistantApi {

    @POST("/api/states/binary_sensor.{device_name}")
    fun createOrUpdateBinarySensor(@Path("device_name") deviceName: String, @Body request: HomeAssistantBinarySensorRequest): Completable

    @DELETE("/api/states/binary_sensor.{device_name}")
    fun deleteBinarySensor(@Path("device_name") deviceName: String): Completable

    @POST("/api/states/sensor.{device_name}")
    fun createOrUpdateSensor(@Path("device_name") deviceName: String, @Body request: HomeAssistantSensorRequest): Completable

    @DELETE("/api/states/sensor.{device_name}")
    fun deleteSensor(@Path("device_name") deviceName: String): Completable
}
