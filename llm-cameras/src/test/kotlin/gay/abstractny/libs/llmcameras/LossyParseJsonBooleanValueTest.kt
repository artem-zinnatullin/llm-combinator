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

class LossyParseJsonBooleanValueTest {

    private val camera = FrigateCamera(FrigateServer("http://myfrigate".toHttpUrl()), "my_camera")
    private val booleanProperty = FrigateCameraLLMPromptPropertyConfig(
        name = "my_property",
        prompt = "my prompt",
        type = BOOLEAN,
    )
    private val promptConfig = FrigateCameraLLMPromptConfig(
        model = "my_model",
        properties = listOf(
            booleanProperty
        )
    )

    @Test
    fun `actual boolean true`() {
        val value = JsonPrimitive(true)
        assertThat(lossyParseJsonBooleanValue(camera, promptConfig, booleanProperty, value))
            .isEqualTo(true)
    }

    @Test
    fun `actual boolean false`() {
        val value = JsonPrimitive(false)
        assertThat(lossyParseJsonBooleanValue(camera, promptConfig, booleanProperty, value))
            .isEqualTo(false)
    }

    @Test
    fun `string yes`() {
        val value = JsonPrimitive("yes")
        assertThat(lossyParseJsonBooleanValue(camera, promptConfig, booleanProperty, value))
            .isEqualTo(true)
    }

    @Test
    fun `string YES`() {
        val value = JsonPrimitive("YES")
        assertThat(lossyParseJsonBooleanValue(camera, promptConfig, booleanProperty, value))
            .isEqualTo(true)
    }

    @Test
    fun `string no`() {
        val value = JsonPrimitive("no")
        assertThat(lossyParseJsonBooleanValue(camera, promptConfig, booleanProperty, value))
            .isEqualTo(false)
    }

    @Test
    fun `string NO`() {
        val value = JsonPrimitive("NO")
        assertThat(lossyParseJsonBooleanValue(camera, promptConfig, booleanProperty, value))
            .isEqualTo(false)
    }

    @Test
    fun `string 0`() {
        val value = JsonPrimitive("0")
        assertThat(lossyParseJsonBooleanValue(camera, promptConfig, booleanProperty, value))
            .isEqualTo(false)
    }

    @Test
    fun `string 1`() {
        val value = JsonPrimitive("1")
        assertThat(lossyParseJsonBooleanValue(camera, promptConfig, booleanProperty, value))
            .isEqualTo(true)
    }

    @Test
    fun `unpredictable`() {
        val value = JsonPrimitive("I AM LLM I AM SO UNPREDICTABLE LOOK AT ME JUST LIKE HUMAN")
        try {
            lossyParseJsonBooleanValue(camera, promptConfig, booleanProperty, value)
            fail("Should have thrown exception")
        } catch (expected: IllegalStateException) {
            assertThat(expected).hasMessage("lossyParseJsonBooleanValue could not decode LLM model 'my_model' response '\"I AM LLM I AM SO UNPREDICTABLE LOOK AT ME JUST LIKE HUMAN\"' as boolean for camera my_camera for property 'my_property'")
        }
    }
}
