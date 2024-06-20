@file:OptIn(ExperimentalEncodingApi::class)

package gay.abstractny.libs.llmcameras

import gay.abstractny.libs.frigate.FrigateCamera
import gay.abstractny.libs.frigate.FrigateServer
import gay.abstractny.libs.frigate.FrigateService
import gay.abstractny.libs.frigate_mqtt.FrigateMqttService
import gay.abstractny.libs.llmcameras.responses.TestCameraLLMResponse
import gay.abstractny.libs.ollama.OllamaGenerateRequest
import gay.abstractny.libs.ollama.OllamaService
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

class LLMCamerasService(
    private val frigateService: FrigateService,
    frigateServers: Set<FrigateServer>,
    private val frigateMqttService: FrigateMqttService,
    private val ollamaService: OllamaService,
) {

    private val frigateCameras = frigateService
        .cameras(frigateServers)
        .cache()


    private val testCam = frigateCameras
        .map { it.single { camera -> camera.name == "test_cam" } }
        .cache()

    private inline fun <reified T : Any> cameraQuery(
        camera: FrigateCamera,
        model: String,
    ): Single<Set<BinarySensor>> {
        return frigateService
            .latestJpg(camera)
            .map { jpegByteArray -> Base64.encode(jpegByteArray) }
            .flatMap { jpegBase64 ->
                ollamaService
                    .generate(
                        OllamaGenerateRequest(
                            model = model,
                            prompt = preparePrompt<T>(),
                            imagesBase64 = listOf(jpegBase64),
                            format = "json",
                        )
                    )
                    .map { Json.decodeFromString<T>(it.response) }
                    .doOnSuccess { println("$it") }
                    .map { cameraLLMResponse -> cameraResponseToSensors(camera, cameraLLMResponse) }
            }
    }

    private fun testCameraQuery(): Single<Set<BinarySensor>> {
        return testCam
            .flatMap { camera ->
                cameraQuery<TestCameraLLMResponse>(
                    camera,
                    model = "llava-llama3:8b",
                )
            }
    }

    fun testCamUpdates(): Flowable<Set<BinarySensor>> {
        return Flowable
            .merge(
                Flowable.interval(2, 30, SECONDS),
                cameraMotionUpdates(testCam),
            )
            .flatMapSingle { testCameraQuery() }
    }

    private fun cameraMotionUpdates(lazyCamera: Single<FrigateCamera>): Flowable<Any> {
        return lazyCamera
            .flatMap { camera -> frigateService.config(camera.server).map { it to camera } }
            .toFlowable()
            .flatMap { (frigateServerConfig, camera) ->
                frigateMqttService.frigateCameraMotionUpdates(
                    frigateServerMqttName = frigateServerConfig.second.mqtt.topicPrefix,
                    camera
                )
            }
    }

    private val cachePrompt = ConcurrentHashMap<KClass<*>, String>()

    private inline fun <reified T : Any> preparePrompt(): String {
        return cachePrompt.getOrPut(T::class) { preparePromptImpl<T>() }
    }

    private inline fun <reified T : Any> preparePromptImpl(): String {
        val propertyPrompt = T::class.declaredMemberProperties
            .associateWith { property ->
                val prompt = property
                    .findAnnotation<LLMPrompt>()
                    ?.prompt
                    ?: error("${T::class} property $property does not have @LLMPromt!")

                prompt
            }

        val propertyJsonName = T::class.declaredMemberProperties
            .associateWith { property ->
                val prompt = property
                    .findAnnotation<SerialName>()
                    ?.value
                    ?: error("${T::class} property $property does not have @SerialName!")

                prompt
            }

        val propertyJsonType = T::class.declaredMemberProperties
            .associateWith { property ->
                property.returnType.classifier!!.toString().substringAfterLast(".")
            }

        // To read properties in exact order they're declared we need to rely on constructor.
        return T::class
            .primaryConstructor!!
            .parameters
            .map { constructorParameter -> T::class.declaredMemberProperties.single { it.name == constructorParameter.name } }
            .joinToString(
                prefix = "You are image analyzer that replies ONLY in VALID JSON with following schema: {",
                postfix = "}. For each JSON property use its name from schema and compute its value as requested type.",
                separator = ", ",
            ) { property ->
                "\"${propertyJsonName[property]}\": \"(${propertyJsonType[property]}) ${propertyPrompt[property]}\""
            }
    }
}
