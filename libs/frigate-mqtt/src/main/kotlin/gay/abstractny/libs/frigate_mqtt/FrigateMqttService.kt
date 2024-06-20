package gay.abstractny.libs.frigate_mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck
import gay.abstractny.libs.frigate.FrigateCamera
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

class FrigateMqttService(mqttHost: String, mqttPort: Int, mqttUsername: String, mqttPassword: String) {

    private val mqttClient = Mqtt5Client
        .builder()
        .simpleAuth(
            Mqtt5SimpleAuth.builder()
                .username(mqttUsername)
                .password(mqttPassword.toByteArray())
                .build()
        )
        .serverHost(mqttHost)
        .serverPort(mqttPort)
        .buildRx();

    private val connect: Single<Mqtt5ConnAck> = mqttClient
        .connect()
        .`as`(RxJavaBridge.toV3Single())
        .cache()

    /**
     * Motion topic looks like this "frigate_server_name/test_camera/motion/state"
     */
    fun frigateCameraMotionUpdates(frigateServerMqttName: String, camera: FrigateCamera): Flowable<Any> {
        return connect
            .toFlowable()
            .flatMap {
                mqttClient
                    .subscribePublishesWith()
                    .topicFilter("$frigateServerMqttName/${camera.name}/motion")
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .applySubscribe()
                    .`as`(RxJavaBridge.toV3Flowable())
                    .doOnNext { println("Camera ${camera.name} Motion ${it.payloadAsBytes.decodeToString()}") }
                    .map { it as Any }
            }
    }
}
