package gay.abstractny.libs.llmcameras

import gay.abstractny.libs.frigate.FrigateCamera
import gay.abstractny.libs.frigate.FrigateServer
import gay.abstractny.libs.yamlconfig.FrigateCameraLLMPromptPropertyConfig
import gay.abstractny.libs.yamlconfig.FrigateCameraLLMPromptPropertyConfig.PropertyType.BOOLEAN
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BinarySensorFriendlyNameTest {

    @Test
    fun getBinarySensorFriendlyName() {
        val camera = FrigateCamera(FrigateServer("http://myfrigate".toHttpUrl()), "my_camera")
        val llmPromptPropertyConfig = FrigateCameraLLMPromptPropertyConfig(
            name = "my_property",
            prompt = "my prompt",
            type = BOOLEAN,
        )
        assertThat(getBinarySensorFriendlyName(camera, llmPromptPropertyConfig)).isEqualTo("My Camera Llm My Property")
    }
}
