package gay.abstractny.libs.homeassistant_websocket

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HomeAssistantWebSocketServiceTest {

    @Test
    fun redactToken() {
        val rawMessage = "{ field_a: no_token_here, field_b: something mytoken something-else, field_c: abc }"
        assertThat(HomeAssistantWebSocketService.redactToken(text = rawMessage, token = "mytoken"))
            .isEqualTo("{ field_a: no_token_here, field_b: something REDACTED_TOKEN something-else, field_c: abc }")
    }
}
