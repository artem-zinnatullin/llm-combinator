package gay.abstractny.libs.llmcameras

import gay.abstractny.libs.frigate.FrigateCamera
import gay.abstractny.libs.yamlconfig.FrigateCameraLLMPromptConfig
import gay.abstractny.libs.yamlconfig.FrigateCameraLLMPromptPropertyConfig
import gay.abstractny.libs.yamlconfig.FrigateCameraLLMPromptPropertyConfig.PropertyType.BOOLEAN
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.util.*

data class BinarySensor(
    val deviceName: String,
    val friendlyName: String,
    val state: Boolean,
)

fun cameraResponseToSensors(
    camera: FrigateCamera,
    promptConfig: FrigateCameraLLMPromptConfig,
    cameraLLMResponse: JsonObject
): Set<BinarySensor> {
    return getBooleanProperties(promptConfig)
        .map { binarySensorProperty ->
            BinarySensor(
                deviceName = getBinarySensorDeviceName(camera, binarySensorProperty),
                friendlyName = getBinarySensorFriendlyName(camera, binarySensorProperty),
                state = lossyParseJsonBooleanFromLLMResponse(camera, promptConfig, binarySensorProperty, cameraLLMResponse),
            )
        }
        .toSet()
}

// TODO add caching.
private fun getBooleanProperties(promptConfig: FrigateCameraLLMPromptConfig): Set<FrigateCameraLLMPromptPropertyConfig> {
    return promptConfig
        .properties
        .filter { it.type == BOOLEAN }
        .toSet()
}

// TODO add caching.
// TODO move out of Cameras module, this is HomeAssistant specific.
private fun getBinarySensorDeviceName(
    camera: FrigateCamera,
    binarySensorProperty: FrigateCameraLLMPromptPropertyConfig
): String {
    return "${camera.name}_llm_${binarySensorProperty.name}}"
}

// TODO add caching.
// TODO move out of Cameras module, this is HomeAssistant specific.
private fun getBinarySensorFriendlyName(
    camera: FrigateCamera,
    binarySensorProperty: FrigateCameraLLMPromptPropertyConfig
): String {
    return getBinarySensorDeviceName(camera, binarySensorProperty)
        .split("_")
        .joinToString(" ") { word ->
            word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
}

// LLMs constantly mess up JSON responses:
//  1. Field name can be mangled but still very close to requested.
//  2. Boolean response can be "yes" or "no" even when directed as true/false.
internal fun lossyParseJsonBooleanFromLLMResponse(
    camera: FrigateCamera,
    promptConfig: FrigateCameraLLMPromptConfig,
    binarySensorProperty: FrigateCameraLLMPromptPropertyConfig,
    cameraLLMResponse: JsonObject
): Boolean {
    val jsonPrimitive = lossyFindJsonProperty(camera, promptConfig, binarySensorProperty, cameraLLMResponse)
    return lossyParseJsonBooleanValue(camera, promptConfig, binarySensorProperty, jsonPrimitive)
}

internal fun lossyFindJsonProperty(camera: FrigateCamera, promptConfig: FrigateCameraLLMPromptConfig, llmPromptPropertyConfig: FrigateCameraLLMPromptPropertyConfig, cameraLLMResponse: JsonObject): JsonPrimitive {
    val directMatch = cameraLLMResponse[llmPromptPropertyConfig.name]

    if (directMatch != null) {
        return directMatch.jsonPrimitive
    }

    // Sometimes LLM adds spaces between '_' in the property name.
    val lossyMatch = cameraLLMResponse
        .keys
        .singleOrNull { key -> key.lowercase().replace(" ", "") == llmPromptPropertyConfig.name }

    if (lossyMatch != null) {
        return cameraLLMResponse[lossyMatch]!!.jsonPrimitive
    }

    error("lossyFindJsonProperty could not find property '${llmPromptPropertyConfig.name}' in LLM model '${promptConfig.model}' response '$cameraLLMResponse' for camera ${camera.name}")
}

internal fun lossyParseJsonBooleanValue(camera: FrigateCamera, promptConfig: FrigateCameraLLMPromptConfig, booleanProperty: FrigateCameraLLMPromptPropertyConfig, shouldBeBooleanValue: JsonPrimitive): Boolean {
    val actualBoolean = shouldBeBooleanValue.booleanOrNull

    if (actualBoolean != null) {
        return actualBoolean
    }

    if (shouldBeBooleanValue.isString) {
        val string = shouldBeBooleanValue.content.lowercase()

        when (string) {
            "yes", "true", "1" -> return true
            "no", "false", "0" -> return false
        }
    }

    error("lossyParseJsonBooleanValue could not decode LLM model '${promptConfig.model}' response '$shouldBeBooleanValue' as boolean for camera ${camera.name} for property '${booleanProperty.name}'")
}
