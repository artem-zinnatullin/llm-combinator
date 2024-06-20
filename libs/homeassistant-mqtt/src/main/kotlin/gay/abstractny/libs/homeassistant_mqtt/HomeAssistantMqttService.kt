package gay.abstractny.libs.homeassistant_mqtt

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.Flowable
import io.reactivex.rxjava3.core.Completable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HomeAssistantMqttService(mqttServer: String, mqttUsername: String, mqttPassword: String) {

    private val mqttClient = Mqtt5Client
        .builder()
        .simpleAuth(
            Mqtt5SimpleAuth.builder()
                .username(mqttUsername)
                .password(mqttPassword.toByteArray())
                .build()
        )
        .serverHost(mqttServer.split(":").first())
        .serverPort(mqttServer.split(":").last().toInt())
        .buildRx();

    /**
     * See https://www.home-assistant.io/integrations/mqtt#discovery-examples
     */
    fun configureDevice(deviceConfiguration: HomeAssistantMqttDeviceConfiguration, baseTopic: String): Completable {
        return mqttClient
            .publish(Flowable.fromCallable {
                Mqtt5Publish
                    .builder()
                    .topic("$baseTopic/${deviceConfiguration.device.name}/config")
                    .contentType("application/json")
                    .payload(Json.encodeToString(deviceConfiguration).toByteArray())
                    .retain(true)
                    .build()
            })
            .`as`(RxJavaBridge.toV3Flowable())
            .ignoreElements()
    }

    /**
     * See https://www.home-assistant.io/integrations/mqtt#discovery-examples
     */
    fun updateState(device: HomeAssistantMqttDevice, baseTopic: String, state: String): Completable {
        return mqttClient
            .publish(Flowable.fromCallable {
                Mqtt5Publish
                    .builder()
                    .topic("$baseTopic/${device.name}/state")
                    .contentType("application/json")
                    .payload(state.toByteArray())
                    .retain(true)
                    .build()
            })
            .`as`(RxJavaBridge.toV3Flowable())
            .ignoreElements()
    }
}
