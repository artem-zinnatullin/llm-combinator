package gay.abstractny.libs.homeassistant_http

import io.reactivex.rxjava3.core.Completable
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

class HomeAssistantHttpService(url: HttpUrl, token: String, debug: Boolean) {

    private val okHttpClient = OkHttpClient
        .Builder()
        .connectTimeout(5, SECONDS)
        .readTimeout(14, SECONDS)
        .writeTimeout(5, SECONDS)
        .callTimeout(15, SECONDS)
        .addInterceptor { chain ->
            // See https://www.home-assistant.io/integrations/http/#http-sensors
            val newRequest = chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()

            chain.proceed(newRequest)
        }
        .apply {
            if (debug) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val homeAssistantApi: HomeAssistantApi = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
        .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .baseUrl(url)
        .build()
        .create(HomeAssistantApi::class.java)

    fun createOrUpdateBinarySensor(deviceName: String, friendlyName: String, state: Boolean): Completable {
        return homeAssistantApi.createOrUpdateBinarySensor(
            deviceName, HomeAssistantBinarySensorRequest(
                state = if (state) "on" else "off",
                attributes = HomeAssistantBinarySensorRequest.Attributes(
                    friendlyName = friendlyName
                )
            )
        )
    }

    fun deleteBinarySensor(deviceName: String): Completable {
        return homeAssistantApi.deleteBinarySensor(deviceName)
    }

    fun createOrUpdateSensor(deviceName: String, friendlyName: String, unitOfMeasurement: String, state: String): Completable {
        return homeAssistantApi.createOrUpdateSensor(deviceName, HomeAssistantSensorRequest(
            state = state,
            attributes = HomeAssistantSensorRequest.Attributes(
                friendlyName = friendlyName,
                unitOfMeasurement = unitOfMeasurement,
            )
        ))
    }

    fun deleteSensor(deviceName: String): Completable {
        return homeAssistantApi.deleteSensor(deviceName)
    }
}
