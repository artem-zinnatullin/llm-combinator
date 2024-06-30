@file:JvmName("Main")
@file:OptIn(ExperimentalEncodingApi::class)

package gay.abstractny.microservices.llmcombinator

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import gay.abstractny.libs.frigate.FrigateServer
import gay.abstractny.libs.frigate.FrigateService
import gay.abstractny.libs.frigate_mqtt.FrigateMqttService
import gay.abstractny.libs.homeassistant_http.HomeAssistantHttpService
import gay.abstractny.libs.homeassistant_websocket.HomeAssistantWebSocketService
import gay.abstractny.libs.llmcameras.LLMCamerasService
import gay.abstractny.libs.ollama.OllamaService
import gay.abstractny.libs.yamlconfig.parseYamlConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Level
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    MainCommand().main(args)
}

class MainCommand : CliktCommand(help = "Run the main code") {

    private val envVarPrefix = "LLM_COMBINATOR"

    init {
        context {
            autoEnvvarPrefix = envVarPrefix
        }
    }

    val yamlConfig by option("--yaml-config")
        .convert { parseYamlConfig(File(it).readText()) }
        .required()

    val homeAssistantToken by option(
        "--homeassistant-token",
        help = "Pass as ${envVarPrefix}_HOMEASSISTANT_TOKEN, see https://developers.home-assistant.io/docs/auth_api/#long-lived-access-token"
    )
        .validate { require(it.isNotEmpty()) }

    val mqttUsername by option("--mqtt-username", help = "Pass as ${envVarPrefix}_MQTT_USERNAME")

    val mqttPassword by option("--mqtt-password", help = "Pass as ${envVarPrefix}_MQTT_PASSWORD")

    override fun run() {
        val logger = KotlinLogging.logger("main")

        RxJavaPlugins.setErrorHandler { throwable ->
            logger.error(throwable) { "Unhandled RxJava exception, crashing the program." }
            // Give a bit of time to flush the logs.
            Thread.sleep(1000)
            exitProcess(2)
        }


        logger.info { "Parsed configuration: $yamlConfig" }

        val frigateServers = yamlConfig.frigate.servers.map { FrigateServer(it.url.toHttpUrl()) }.toSet()
        val frigateService = FrigateService(debug = false)
        val frigateMqttService =
            FrigateMqttService(
                mqttHost = yamlConfig.mqtt.host,
                mqttPort = yamlConfig.mqtt.port,
                mqttUsername = mqttUsername!!,
                mqttPassword = mqttPassword!!
            )
        val ollamaService = OllamaService(yamlConfig.ollama.url.toHttpUrl(), Level.INFO)
        val llmCamerasService = LLMCamerasService(
            frigateService,
            frigateServers,
            frigateMqttService,
            ollamaService,
            yamlConfig.frigate.cameras
        )
        val homeAssistantUrl = yamlConfig.homeAssistant.url.toHttpUrl()
        val homeAssistantHttpService = HomeAssistantHttpService(
            homeAssistantUrl,
            token = homeAssistantToken!!,
        )

        val homeAssistantWebSocketService = HomeAssistantWebSocketService(
            homeAssistantUrl,
            token = homeAssistantToken!!,
        )

//        homeAssistantWebSocketService
//            .subscribeToTrigger(Trigger(
//                platform = "state",
//                entityId = "binary_sensor.office_motion_sensor_occupancy"
//            ))
//            .doOnNext {
//                logger.info { "websocket subscribeToTrigger(): $it" }
//            }
//            .blockingSubscribe()

        llmCamerasService
            .camerasUpdates()
            .doOnNext { logger.info { ("Update: $it") } }
            .flatMapCompletable { binarySensorsUpdate ->
                Completable
                    .merge(binarySensorsUpdate
                        .map { binarySensor ->
                            if (yamlConfig.homeAssistant.updateSensors) {
                                homeAssistantHttpService.createOrUpdateBinarySensor(
                                    deviceName = binarySensor.deviceName,
                                    friendlyName = binarySensor.friendlyName,
                                    state = binarySensor.state
                                )
                            } else {
                                Completable.complete()
                            }
                        }
                    )
            }
            .blockingSubscribe()
    }
}

