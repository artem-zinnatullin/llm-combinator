@file:OptIn(ExperimentalEncodingApi::class)

package gay.abstractny.libs.llmcameras

import gay.abstractny.libs.frigate.FrigateCamera
import gay.abstractny.libs.frigate.FrigateServer
import gay.abstractny.libs.frigate.FrigateService
import gay.abstractny.libs.frigate_mqtt.FrigateMqttService
import gay.abstractny.libs.ollama.OllamaGenerateRequest
import gay.abstractny.libs.ollama.OllamaService
import gay.abstractny.libs.yamlconfig.FrigateCameraConfig
import gay.abstractny.libs.yamlconfig.FrigateCameraLLMPromptConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
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

    private val logger = KotlinLogging.logger("LLMCamerasService")

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
                Flowable.merge(frigateCameraConfig.llmPrompts.map { Flowable.fromCallable { camera to it } })
            }
            .flatMap { (camera, llmPromptConfig) ->
                val signals = mutableListOf<Flowable<*>>()

                signals += Flowable.interval(
                    Random.nextLong(frigateCamerasConfig.size.toLong()),
                    frigateCameraConfig.periodicUpdateSec.toLong(),
                    SECONDS
                )

                if (llmPromptConfig.motionUpdates.enabled) {
                    signals += cameraMotionUpdates(camera)
                }

                Flowable
                    .merge(signals)
                    .flatMapSingle { cameraQuery(camera, llmPromptConfig) }
            }
    }

    // TODO internal
    fun cameraQueryRaw(
        camera: FrigateCamera,
        llmPromptConfig: FrigateCameraLLMPromptConfig,
    ): Single<JsonObject> {
        return frigateService
            .latestJpg(camera)
            .map { jpegByteArray -> Base64.encode(jpegByteArray) }
            .flatMap { jpegBase64 ->
                ollamaService
                    .generate(
                        OllamaGenerateRequest(
                            model = llmPromptConfig.model,
                            prompt = preparePrompt(llmPromptConfig),
                            imagesBase64 = listOf(jpegBase64),
                            format = "json",
                            //options = OllamaGenerateRequest.Options(),
                        )
                    )
                    .map { Json.decodeFromString<JsonObject>(it.response) }
                    .doOnSuccess { logger.debug { "cameraQuery response: $it" } }
            }
    }

    private fun cameraQuery(
        camera: FrigateCamera,
        llmPromptConfig: FrigateCameraLLMPromptConfig,
    ): Single<Set<BinarySensor>> {
        return cameraQueryRaw(camera, llmPromptConfig)
            .map { cameraLLMResponse -> cameraResponseToSensors(camera, llmPromptConfig, cameraLLMResponse) }
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

    private val cachePrompt = ConcurrentHashMap<FrigateCameraLLMPromptConfig, String>()

    private fun preparePrompt(llmPromptConfig: FrigateCameraLLMPromptConfig): String {
        return cachePrompt.getOrPut(llmPromptConfig) { preparePromptImpl(llmPromptConfig) }
    }

    private fun preparePromptImpl(llmPromptConfig: FrigateCameraLLMPromptConfig): String {
        // Properties must be put in exact order they're declared otherwise prompt will produce unexpected result.
        return llmPromptConfig
            .properties
            .joinToString(
                prefix = llmPromptConfig.prefix,
                postfix = llmPromptConfig.postfix,
                separator = ", ",
            ) { property ->
                "\"${property.name}\": \"(${property.type}) ${property.prompt}\""
            }
    }
}
