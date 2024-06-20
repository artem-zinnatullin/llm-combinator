@file:OptIn(ExperimentalEncodingApi::class)

package gay.abstractny.libs.llmcameras

import gay.abstractny.libs.frigate.FrigateCamera
import gay.abstractny.libs.frigate.FrigateServer
import gay.abstractny.libs.frigate.FrigateService
import gay.abstractny.libs.frigate_mqtt.FrigateMqttService
import gay.abstractny.libs.ollama.OllamaGenerateRequest
import gay.abstractny.libs.ollama.OllamaService
import gay.abstractny.libs.yamlconfig.FrigateCameraConfig
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

class LLMCamerasService(
    private val frigateService: FrigateService,
    private val frigateServers: Set<FrigateServer>,
    private val frigateMqttService: FrigateMqttService,
    private val ollamaService: OllamaService,
    private val frigateCamerasConfig: List<FrigateCameraConfig>,
) {

    private val frigateCameras = frigateService
        .cameras(frigateServers)
        .cache()

    fun camerasUpdates(): Flowable<Set<BinarySensor>> {
        return Flowable.merge(frigateCamerasConfig.map { cameraUpdates(it) })
    }

    private fun cameraUpdates(frigateCameraConfig: FrigateCameraConfig): Flowable<Set<BinarySensor>> {
        return frigateCameras
            .map { cameras ->
                cameras
                    .singleOrNull { it.name == frigateCameraConfig.name }
                    ?: error("Cannot find frigate camera with name ${frigateCameraConfig.name} among cameras through all frigate servers $frigateServers: ${cameras.map { it.name }}")
            }
            .toFlowable()
            .flatMap { camera ->
                val signals = mutableListOf<Flowable<*>>()

                signals += Flowable.interval(
                    Random.nextLong(frigateCamerasConfig.size.toLong()),
                    frigateCameraConfig.periodicUpdateSec.toLong(),
                    SECONDS
                )

                if (frigateCameraConfig.motionUpdates.enabled) {
                    signals += cameraMotionUpdates(camera)
                }

                Flowable
                    .merge(signals)
                    .map { camera }
            }
            .flatMapSingle { camera ->
                cameraQuery(camera, frigateCameraConfig)
            }
    }

    private fun cameraQuery(
        camera: FrigateCamera,
        frigateCameraConfig: FrigateCameraConfig,
    ): Single<Set<BinarySensor>> {
        return frigateService
            .latestJpg(camera)
            .map { jpegByteArray -> Base64.encode(jpegByteArray) }
            .flatMap { jpegBase64 ->
                ollamaService
                    .generate(
                        OllamaGenerateRequest(
                            model = frigateCameraConfig.llmPrompt.model,
                            prompt = preparePrompt(frigateCameraConfig),
                            imagesBase64 = listOf(jpegBase64),
                            format = "json",
                        )
                    )
                    .map { Json.decodeFromString<JsonElement>(it.response) }
                    .doOnSuccess { println("$it") }
                    .map { cameraLLMResponse -> cameraResponseToSensors(camera, cameraLLMResponse) }
            }
    }

    private fun cameraMotionUpdates(camera: FrigateCamera): Flowable<Any> {
        return frigateService
            .config(camera.server)
            .map { it to camera }
            .toFlowable()
            .flatMap { (frigateServerConfig, camera) ->
                frigateMqttService.frigateCameraMotionUpdates(
                    frigateServerMqttName = frigateServerConfig.second.mqtt.topicPrefix,
                    camera
                )
            }
    }

    private val cachePrompt = ConcurrentHashMap<FrigateCameraConfig, String>()

    private fun preparePrompt(frigateCameraConfig: FrigateCameraConfig): String {
        return cachePrompt.getOrPut(frigateCameraConfig) { preparePromptImpl(frigateCameraConfig) }
    }

    private fun preparePromptImpl(frigateCameraConfig: FrigateCameraConfig): String {
        // Properties must be put in exact order they're declared otherwise prompt might become unstable.
        return frigateCameraConfig
            .llmPrompt
            .properties
            .joinToString(
                prefix = frigateCameraConfig.llmPrompt.prefix,
                postfix = frigateCameraConfig.llmPrompt.postfix,
                separator = ", ",
            ) { property ->
                "\"${property.name}\": \"(${property.type}) ${property.prompt}\""
            }
    }
}
