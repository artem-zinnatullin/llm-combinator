package gay.abstractny.libs.homeassistant_websocket

import gay.abstractny.libs.homeassistant_websocket.requests.AuthRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicBoolean

class HomeAssistantWebSocketService(private val homeAssistantUrl: HttpUrl, val token: String) {

    companion object {
        private val logger = KotlinLogging.logger(HomeAssistantWebSocketService::class.java.simpleName)
        internal fun redactToken(text: String, token: String) = text.replace(token, "REDACTED_TOKEN")
    }

    private val okHttpClient = OkHttpClient
        .Builder()
        .connectTimeout(5, SECONDS)
        .readTimeout(14, SECONDS)
        .writeTimeout(5, SECONDS)
        .callTimeout(15, SECONDS)
        .apply {
            addInterceptor(HttpLoggingInterceptor {
                logger.debug { it }
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // 1. [x] Shared() Flowable websocket
    // 2. [x] Authentication as part of shared WebSocket
    // 3. Allow sending messages and receiving stream of response(s)

    data class AuthenticatedWebSocket(
        val webSocket: WebSocket,
        val message: String?,
    )

    private class LoggingWebSocket(private val actualWebSocket: WebSocket, private val token: String) :
        WebSocket by actualWebSocket {
        override fun send(text: String): Boolean {
            val message = redactToken(text, token)
            logger.info { "WebSocket -> send: $message" }
            return actualWebSocket.send(text)
        }
    }

    val authenticatedWebSocket: Flowable<AuthenticatedWebSocket> = Flowable
        .create({ source ->
            val authOk = AtomicBoolean()

            val webSocketListener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    logger.info { "WebSocket <- onOpen: response=$response" }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    actualOnMessage(LoggingWebSocket(webSocket, token), redactToken(text, token))
                }

                fun actualOnMessage(webSocket: WebSocket, message: String) {
                    logger.info { "WebSocket <- onMessage: message=$message" }

                    val jsonMessage = json.decodeFromString<JsonObject>(message)

                    when (jsonMessage["type"]?.jsonPrimitive?.content) {
                        "auth_required" -> {
                            webSocket.send(json.encodeToString(AuthRequest(accessToken = token)))
                        }

                        "auth_ok" -> {
                            when (authOk.compareAndSet(false, true)) {
                                true -> source.onNext(AuthenticatedWebSocket(webSocket, null))
                                false -> source.onError(IllegalStateException("auth_ok internal state does not match HomeAssistant response"))
                            }
                        }

                        else -> {
                            if (authOk.get()) {
                                source.onNext(AuthenticatedWebSocket(webSocket, message))
                            }
                        }
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    logger.error(t) { "WebSocket <- onFailure: response=$response" }
                    source.onError(t)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    logger.info { "WebSocket <- onClosed: code=$code, reason=$reason" }
                    source.onComplete()
                }
            }

            logger.info { "WebSocket -> newWebSocket" }

            val webSocket = okHttpClient.newWebSocket(
                Request.Builder()
                    .url(
                        homeAssistantUrl.newBuilder()
                            .addPathSegment("api")
                            .addPathSegment("websocket")
                            .build()
                    )
                    .build(),
                webSocketListener
            )

            source.setCancellable { webSocket.close(1001, null) }
        }, BackpressureStrategy.BUFFER)
        .share()

}

