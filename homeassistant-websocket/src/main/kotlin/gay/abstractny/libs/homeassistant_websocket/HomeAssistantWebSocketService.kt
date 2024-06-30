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

class HomeAssistantWebSocketService(private val homeAssistantUrl: HttpUrl, val token: String) {

    private val logger = KotlinLogging.logger(HomeAssistantWebSocketService::class.java.simpleName)

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

    // 1. Shared() Flowable websocket
    // 2. Authentication as part of shared WebSocket
    // 3.

//    private val authenticatedWebSocket = Flowable
//        .create<Pair<WebSocket, Any>>()
//        .share()

    fun connect(): Flowable<Pair<WebSocket, Any>> {
        return Flowable
            .create<Pair<WebSocket, String>>(
                { source ->


                    val webSocketListener = object : WebSocketListener() {
                        override fun onOpen(webSocket: WebSocket, response: Response) {
                            logger.info { "WebSocket: onOpen, response=$response" }
                        }

                        override fun onMessage(webSocket: WebSocket, message: String) {
                            // TODO: check if this can break the message format.
                            val preProcessedMessage = message.replace(token, "REDACTED_TOKEN")
                            logger.info { "WebSocket: onMessage, message=$preProcessedMessage" }
                            source.onNext(webSocket to preProcessedMessage)
                        }

                        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                            source.onError(t)
                        }

                        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                            source.onComplete()
                        }
                    }

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
                }, BackpressureStrategy.BUFFER
            )
            .map { (ws, message) -> ws to json.decodeFromString<JsonObject>(message) }
            .doOnNext { (ws, message) ->
                if (message["type"]?.jsonPrimitive?.content == "auth_required") {
                    ws.send(json.encodeToString(AuthRequest(accessToken = token)))
                }
            }
            .map { it as Pair<WebSocket, Any> }
    }

    fun binarySensorUpdates(sensorName: String): Flowable<Boolean> {
        TODO()
    }
}

