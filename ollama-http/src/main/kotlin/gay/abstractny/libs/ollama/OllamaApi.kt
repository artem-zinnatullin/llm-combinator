package gay.abstractny.libs.ollama

import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * See https://github.com/ollama/ollama/blob/main/docs/api.md
 */
internal interface OllamaApi {

    @POST("api/generate")
    fun generate(@Body request: OllamaGenerateRequest): Single<OllamaGenerateResponse>
}
