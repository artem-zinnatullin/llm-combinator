package gay.abstractny.libs.yamlconfig

import gay.abstractny.libs.yamlconfig.FrigateCameraLLMPromptPropertyConfig.PropertyType.BOOLEAN
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
      llm_prompts:
        - model: "llava-llama3:8b-1"
          properties:
            - name: object_1_present
              prompt: Analyze the image for presence of object 1
              type: boolean
      periodic_update_sec: 45
    - name: my_cam_2
      llm_prompts:
        - model: "llava-llama3:8b-2"
          properties:
            - name: object_2_present
              prompt: Analyze the image for presence of object 2
              type: boolean
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
                            llmPrompts = listOf(
                                FrigateCameraLLMPromptConfig(
                                    model = "llava-llama3:8b-1",
                                    properties = listOf(
                                        FrigateCameraLLMPromptPropertyConfig(
                                            name = "object_1_present",
                                            prompt = "Analyze the image for presence of object 1",
                                            type = BOOLEAN,
                                        )
                                    ),
                                    motionUpdates = FrigateCameraMotionConfig(
                                        enabled = true,
                                    ),
                                )
                            ),
                            periodicUpdateSec = 45,
                        ),
                        FrigateCameraConfig(
                            name = "my_cam_2",
                            llmPrompts = listOf(
                                FrigateCameraLLMPromptConfig(
                                    model = "llava-llama3:8b-2",
                                    properties = listOf(
                                        FrigateCameraLLMPromptPropertyConfig(
                                            name = "object_2_present",
                                            prompt = "Analyze the image for presence of object 2",
                                            type = BOOLEAN,
                                        )
                                    ),
                                    motionUpdates = FrigateCameraMotionConfig(
                                        enabled = true,
                                    ),
                                )
                            ),
                            periodicUpdateSec = 15,
                        )
                    )
                )
            )
        )
    }
}
