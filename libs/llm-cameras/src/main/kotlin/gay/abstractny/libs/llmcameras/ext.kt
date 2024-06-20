package gay.abstractny.libs.llmcameras

import gay.abstractny.libs.frigate.FrigateCamera
import kotlinx.serialization.SerialName
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

data class BinarySensor(
    val deviceName: String,
    val friendlyName: String,
    val state: Boolean,
)

@Target(PROPERTY)
annotation class LLMPrompt(
    val prompt: String,
)

const val EMERGENCY_PROMPT = "If there is a sign of fire, smoke, water leak or other major emergency — set this to true"
const val DESCRIPTION_PROMPT = "Set short description of what is going on in the image"

private val cacheBinarySensorProperties = ConcurrentHashMap<KClass<Any>, Set<KProperty<Any>>>()
private val cachePropertySerialName = ConcurrentHashMap<KProperty<Any>, String>()
private val cacheBinarySensorDeviceName = ConcurrentHashMap<KProperty<Any>, String>()
private val cacheBinarySensorFriendlyName = ConcurrentHashMap<KProperty<Any>, String>()

fun cameraResponseToSensors(camera: FrigateCamera, cameraLLMResponse: Any): Set<BinarySensor> {
    val cameraLLMResponseClass = cameraLLMResponse.javaClass.kotlin

    return getBinarySensorProperties(cameraLLMResponseClass)
        .map { binarySensorProperty ->
            BinarySensor(
                deviceName = getBinarySensorDeviceName(camera, binarySensorProperty),
                friendlyName = getBinarySensorFriendlyName(camera, binarySensorProperty),
                state = binarySensorProperty.javaField!!.getBoolean(cameraLLMResponse)
            )
        }
        .toSet()
}

private fun getBinarySensorProperties(cameraLLMResponseClass: KClass<Any>): Set<KProperty<Any>> =
    cacheBinarySensorProperties
        .getOrPut(cameraLLMResponseClass) {
            cameraLLMResponseClass
                .memberProperties
                .filter { it.returnType.classifier == Boolean::class }
                .map {
                    @Suppress("UNCHECKED_CAST")
                    it as KProperty<Any>
                }
                .onEach { property ->
                    cachePropertySerialName.getOrPut(property) {
                        property.findAnnotation<SerialName>()!!.value
                    }
                    property.isAccessible = true
                }
                .toSet()
        }

private fun getBinarySensorDeviceName(camera: FrigateCamera, binarySensorProperty: KProperty<Any>): String {
    return cacheBinarySensorDeviceName.getOrPut(binarySensorProperty) {
        "${camera.name}_llm_${cachePropertySerialName[binarySensorProperty]}"
    }
}

private fun getBinarySensorFriendlyName(camera: FrigateCamera, binarySensorProperty: KProperty<Any>): String {
    return cacheBinarySensorFriendlyName.getOrPut(binarySensorProperty) {
        getBinarySensorDeviceName(camera, binarySensorProperty)
            .split("_")
            .joinToString(" ") { word ->
                word.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            }
    }
}
