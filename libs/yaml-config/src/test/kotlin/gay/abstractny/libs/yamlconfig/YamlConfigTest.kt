package gay.abstractny.libs.yamlconfig

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class YamlConfigTest {

    @Test
    fun `parses good YAML config without optional fields`() {
        assertThat(
            parseYamlConfig(
                """
ollama:
  url: http://myollama:1000

home_assistant:
  url: http://myhomassistant:1001

mqtt:
  host: mymqtt
  port: 1002

frigate:
  servers:
    - url: http://myfrigate1:1003/
    - url: http://myfrigate2:1004/
  cameras:
    - name: my_cam_1
      periodic_update_sec: 45
    - name: my_cam_2
      periodic_update_sec: 15
"""
            )
        ).isEqualTo(
            YamlConfig(
                ollama = OllamaConfig(
                    url = "http://myollama:1000",
                ),
                homeAssistant = HomeAssistantConfig(
                    url = "http://myhomassistant:1001",
                ),
                mqtt = MqttConfig(
                    host = "mymqtt",
                    port = 1002,
                ),
                frigate = FrigateConfig(
                    servers = listOf(
                        FrigateServerConfig(
                            url = "http://myfrigate1:1003/",
                        ),
                        FrigateServerConfig(
                            url = "http://myfrigate2:1004/"
                        ),
                    ),
                    cameras = listOf(
                        FrigateCameraConfig(
                            name = "my_cam_1",
                            periodicUpdateSec = 45,
                            motionUpdates = FrigateCameraMotionConfig(
                                enabled = true,
                            ),
                        ),
                        FrigateCameraConfig(
                            name = "my_cam_2",
                            periodicUpdateSec = 15,
                            motionUpdates = FrigateCameraMotionConfig(
                                enabled = true,
                            )
                        )
                    )
                )
            )
        )
    }
}
