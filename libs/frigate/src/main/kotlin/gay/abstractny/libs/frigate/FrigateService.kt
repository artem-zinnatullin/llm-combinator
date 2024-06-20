package gay.abstractny.libs.frigate

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit.SECONDS

class FrigateService(debug: Boolean) {

    private val okHttpClient = OkHttpClient
        .Builder()
        .connectTimeout(5, SECONDS)
        .readTimeout(14, SECONDS)
        .writeTimeout(5, SECONDS)
        .callTimeout(15, SECONDS)
        .apply {
            if (debug) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }
        .build()

    private val frigateApis: MutableMap<FrigateServer, FrigateApi> = ConcurrentHashMap()

    private val json = Json { ignoreUnknownKeys = true }

    private fun frigateApi(server: FrigateServer): FrigateApi {
        return frigateApis.computeIfAbsent(server) {
            Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .baseUrl(server.serverUrl)
                .build()
                .create(FrigateApi::class.java)
        }
    }

    fun config(server: FrigateServer): Single<Pair<FrigateServer, FrigateConfig>> {
        return frigateApi(server).config().map { config -> server to config }
    }

    fun latestJpg(camera: FrigateCamera): Single<ByteArray> {
        return frigateApi(camera.server)
            .latestJpg(camera.name)
            .map {
                val bytes = it.bytes()
                require(bytes.isNotEmpty()) { "latestJpg for camera ${camera.name} is 0 bytes!" }
                bytes
            }
    }

    fun cameras(servers: Set<FrigateServer>): Single<Set<FrigateCamera>> {
        return Single
            .zip(servers.map { server -> config(server) }) { configs ->
                @Suppress("UNCHECKED_CAST")
                configs.toList() as List<Pair<FrigateServer, FrigateConfig>>
            }
            .map { configs ->
                configs.flatMap { (server, config) ->
                    config.cameras.map { cameraConfig ->
                        FrigateCamera(
                            server,
                            cameraConfig.key
                        )
                    }
                }.toSet()
            }
    }
}
