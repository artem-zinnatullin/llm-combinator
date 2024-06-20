@file:JvmName("Main")
@file:OptIn(ExperimentalEncodingApi::class)

package gay.abstractny.microservices.llmcombinator

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.transformAll
import com.github.ajalt.clikt.parameters.options.validate
import gay.abstractny.libs.frigate.FrigateServer
import gay.abstractny.libs.frigate.FrigateService
import gay.abstractny.libs.frigate_mqtt.FrigateMqttService
import gay.abstractny.libs.homeassistant_http.HomeAssistantHttpService
import gay.abstractny.libs.llmcameras.BinarySensor
import gay.abstractny.libs.llmcameras.LLMCamerasService
import gay.abstractny.libs.ollama.OllamaService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Level
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import okhttp3.HttpUrl.Companion.toHttpUrl
import kotlin.io.encoding.ExperimentalEncodingApi

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

    val ollamaUrl by option("--ollama-url")
        .convert { it.toHttpUrl() }
        .required()

    val homeAssistantUrl by option("--homeassistant-url")
        .convert { it.toHttpUrl() }
        .required()

    val homeAssistantToken by option("--homeassistant-token", help = "Pass as ${envVarPrefix}_HOMEASSISTANT_TOKEN")
        .validate { require(it.isNotEmpty()) }

    val homeAssistantUpdateSensors by option(
        "--homeassistant-update-sensors",
        help = "Useful for debugging to turn off locally"
    )
        .convert { it.toBoolean() }
        .default(true)

    val frigateUrls by option("--frigate-urls")
        .transformAll { urls -> urls.flatMap { it.split(",").map { it.toHttpUrl() } } }
        .validate { require(it.isNotEmpty()) }

    val mqttServer by option("--mqtt-server", help = "ip:port")
        .validate { require(it.contains(":")) }

    val mqttUsername by option("--mqtt-username")

    val mqttPassword by option("--mqtt-password", help = "Pass as ${envVarPrefix}_MQTT_PASSWORD")

    override fun run() {
        val logger = KotlinLogging.logger("main")

        val frigateServers = frigateUrls.map { FrigateServer(it) }.toSet()
        val frigateService = FrigateService(debug = false)
        val frigateMqttService =
            FrigateMqttService(mqttServer = mqttServer!!, mqttUsername = mqttUsername!!, mqttPassword = mqttPassword!!)
        val ollamaService = OllamaService(ollamaUrl, Level.INFO)
        val llmCamerasService = LLMCamerasService(frigateService, frigateServers, frigateMqttService, ollamaService)
        val homeAssistantHttpService =
            HomeAssistantHttpService(homeAssistantUrl, token = homeAssistantToken!!, debug = false)

        llmCamerasService
            .testCamUpdates()
            .doOnNext { logger.info { ("Update: $it") } }
            .flatMapCompletable { binarySensorsUpdate ->
                Completable
                    .merge(binarySensorsUpdate
                        .map { binarySensor ->
                            if (homeAssistantUpdateSensors) {
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

