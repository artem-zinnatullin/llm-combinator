package gay.abstractny.libs.frigate

import io.reactivex.rxjava3.core.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * See https://docs.frigate.video/integrations/api
 */
internal interface FrigateApi {

    @GET("api/config")
    fun config(): Single<FrigateConfig>

    @GET("api/{camera_name}/latest.jpg")
    fun latestJpg(@Path("camera_name") cameraName: String): Single<ResponseBody>

}
