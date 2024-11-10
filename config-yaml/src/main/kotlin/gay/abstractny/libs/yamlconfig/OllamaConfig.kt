package gay.abstractny.libs.yamlconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OllamaConfig(
    @SerialName("url")
    val url: String,

    @SerialName("socket_connect_timeout_ms")
    val socketConnectTimeoutMs: Long = 10_000,

    @SerialName("socket_read_timeout_ms")
    val socketReadTimeoutMs: Long = 30_000,

    @SerialName("socket_write_timeout_ms")
    val socketWriteTimeoutMs: Long = 30_000,

    @SerialName("call_timeout_ms")
    val callTimeoutMs: Long = 30_000,
)
