package gay.abstractny.libs.ollama

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Level
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit.SECONDS

class OllamaService(url: HttpUrl, private val loggingLevel: Level) {

    private val logger = KotlinLogging.logger("Ollama")

    private val okHttpClient = OkHttpClient
        .Builder()
        .connectTimeout(5, SECONDS)
        .readTimeout(14, SECONDS)
        .writeTimeout(5, SECONDS)
        .callTimeout(15, SECONDS)
        .apply {
            if (loggingLevel.toInt() <= Level.TRACE.toInt()) {
                addInterceptor(HttpLoggingInterceptor { logger.trace { it } }.apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val ollamaApi: OllamaApi = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
        .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .baseUrl(url)
        .build()
        .create(OllamaApi::class.java)

    fun generate(request: OllamaGenerateRequest): Single<OllamaGenerateResponse> {
        return Single
            .fromCallable {
                val startTimeMs = System.currentTimeMillis()
                if (loggingLevel.toInt() <= Level.DEBUG.toInt()) {
                    logger.debug { "-> Request $request" }
                }
                startTimeMs
            }
            .flatMap { startTimeMs -> ollamaApi.generate(request).map { it to startTimeMs } }
            .doOnSuccess { (response, startTimeMs) ->
                if (loggingLevel.toInt() <= Level.INFO.toInt()) {
                    logger.info { "<- Response: $response took ${System.currentTimeMillis() - startTimeMs}ms" }
                }
            }
            .map { (response, _) -> response }
    }
}
