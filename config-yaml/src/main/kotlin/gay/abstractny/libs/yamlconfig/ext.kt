package gay.abstractny.libs.yamlconfig

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import org.intellij.lang.annotations.Language

@Suppress("TooGenericExceptionCaught")
fun parseYamlConfig(@Language("yaml") content: String): YamlConfig {
    return try {
        Yaml
            .default
            .decodeFromString<YamlConfig>(content)
            .let { applyFrigateDefaultLLMModel(it) }
    } catch (e: Throwable) {
        // This message clearly points to a YAML structure issue for user.
        System.err.println(e)
        throw e
    }
}

@Suppress("MaxLineLength")
private fun applyFrigateDefaultLLMModel(originalConfig: YamlConfig): YamlConfig {
    val updatedCameras = originalConfig
        .frigate
        .cameras
        .map { camera ->
            val updatedLLMPrompts = camera
                .llmPrompts
                .map { llmPrompt ->
                    val updatedModel = if (llmPrompt.model.trim().isEmpty()) {
                        if (originalConfig.frigate.defaultLLMModel.isNullOrEmpty()) {
                            error("frigate.cameras.${camera.name} must have llm_prompts.model declared or frigate.default_llm_model must be set")
                        } else {
                            originalConfig.frigate.defaultLLMModel!!
                        }
                    } else {
                        llmPrompt.model
                    }

                    llmPrompt.copy(model = updatedModel)
                }

            camera.copy(llmPrompts = updatedLLMPrompts)
        }

    return originalConfig.copy(frigate = originalConfig.frigate.copy(cameras = updatedCameras))
}
