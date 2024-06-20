package gay.abstractny.libs.yamlconfig

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import org.intellij.lang.annotations.Language

fun parseYamlConfig(@Language("yaml") content: String): YamlConfig {
    var yamlConfig = try {
        Yaml.default.decodeFromString<YamlConfig>(content)
    } catch (e: Throwable) {
        // This message clearly points to a YAML structure issue for user.
        System.err.println(e)
        throw e
    }

    val updatedFrigateCameras = yamlConfig
        .frigate
        .cameras
        .map { camera ->
            // If model is not set in camera config we must use default model from common config.
            val updatedModel = if (camera.llmPrompt.model.trim().isEmpty()) {
                if (yamlConfig.frigate.defaultLLMModel == null) {
                    error("frigate.cameras.${camera.name} must have llm_prompt.model declared or frigate.default_llm_model must be set")
                } else {
                    yamlConfig.frigate.defaultLLMModel!!
                }
            } else {
                camera.llmPrompt.model
            }

            camera.copy(llmPrompt = camera.llmPrompt.copy(model = updatedModel))
        }

    yamlConfig = yamlConfig.copy(frigate = yamlConfig.frigate.copy(cameras = updatedFrigateCameras))

    return yamlConfig
}
