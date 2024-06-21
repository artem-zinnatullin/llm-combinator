package gay.abstractny.libs.llmcameras

import gay.abstractny.libs.frigate.FrigateCamera
import gay.abstractny.libs.frigate.FrigateServer
import gay.abstractny.libs.yamlconfig.FrigateCameraLLMPromptConfig
import gay.abstractny.libs.yamlconfig.FrigateCameraLLMPromptPropertyConfig
import gay.abstractny.libs.yamlconfig.FrigateCameraLLMPromptPropertyConfig.PropertyType.BOOLEAN
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class LossyFindJsonPropertyTest {

    private val camera = FrigateCamera(FrigateServer("http://myfrigate".toHttpUrl()), "my_camera")
    private val promptConfig = FrigateCameraLLMPromptConfig(
        model = "my_model",
        properties = listOf(
            FrigateCameraLLMPromptPropertyConfig(
                name = "my_property",
                prompt = "my prompt",
                type = BOOLEAN,
            )
        )
    )

    @Test
    fun `direct match`() {
        val value = JsonPrimitive(true)
        val cameraLLMResponse = JsonObject(mapOf("my_property" to value, "my_other_property" to JsonPrimitive(123)))

        assertThat(lossyFindJsonProperty(camera, promptConfig, promptConfig.properties.first(), cameraLLMResponse))
            .isEqualTo(value)
    }

    @Test
    fun `LLM added space to property name`() {
        val value = JsonPrimitive(true)
        val cameraLLMResponse = JsonObject(mapOf("my _property" to value, "my _other_property" to JsonPrimitive(123)))

        assertThat(lossyFindJsonProperty(camera, promptConfig, promptConfig.properties.first(), cameraLLMResponse))
            .isEqualTo(value)
    }

    @Test
    fun `LLM UPPERCASED property name`() {
        val value = JsonPrimitive(true)
        val cameraLLMResponse = JsonObject(mapOf("MY_PROPERTY" to value, "MY_OTHER_PROPERTY" to JsonPrimitive(123)))

        assertThat(lossyFindJsonProperty(camera, promptConfig, promptConfig.properties.first(), cameraLLMResponse))
            .isEqualTo(value)
    }

    @Test
    fun `LLM UPPERCASED property name and added space`() {
        val value = JsonPrimitive(true)
        val cameraLLMResponse = JsonObject(mapOf("MY _PROPERTY" to value, "MY _OTHER_PROPERTY" to JsonPrimitive(123)))

        assertThat(lossyFindJsonProperty(camera, promptConfig, promptConfig.properties.first(), cameraLLMResponse))
            .isEqualTo(value)
    }

    @Test
    fun `LLM changed property name unpredictably`() {
        val value = JsonPrimitive(true)
        val cameraLLMResponse = JsonObject(mapOf("my_property_unpredictable" to value, "my_other_property" to JsonPrimitive(123)))

        try {
            lossyFindJsonProperty(camera, promptConfig, promptConfig.properties.first(), cameraLLMResponse)
            fail("Should have thrown exception")
        } catch (expected: IllegalStateException) {
            assertThat(expected).hasMessage("lossyFindJsonProperty could not find property 'my_property' in LLM model 'my_model' response '{\"my_property_unpredictable\":true,\"my_other_property\":123}' for camera my_camera")
        }
    }
}
