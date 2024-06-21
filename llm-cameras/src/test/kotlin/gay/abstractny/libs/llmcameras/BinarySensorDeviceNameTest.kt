package gay.abstractny.libs.llmcameras

import gay.abstractny.libs.frigate.FrigateCamera
import gay.abstractny.libs.frigate.FrigateServer
import gay.abstractny.libs.yamlconfig.FrigateCameraLLMPromptPropertyConfig
import gay.abstractny.libs.yamlconfig.FrigateCameraLLMPromptPropertyConfig.PropertyType.BOOLEAN
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BinarySensorDeviceNameTest {

    @Test
    fun getBinarySensorDeviceName() {
        val camera = FrigateCamera(FrigateServer("http://myfrigate".toHttpUrl()), "my_camera")
        val llmPromptPropertyConfig = FrigateCameraLLMPromptPropertyConfig(
            name = "my_property",
            prompt = "my prompt",
            type = BOOLEAN,
        )
        assertThat(getBinarySensorDeviceName(camera, llmPromptPropertyConfig)).isEqualTo("my_camera_llm_my_property")
    }
}
