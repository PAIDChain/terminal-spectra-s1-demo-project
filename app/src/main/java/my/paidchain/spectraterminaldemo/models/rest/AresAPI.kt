package my.paidchain.spectraterminaldemo.models.rest

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface AresAPI {
    @POST("/tx/android-rest-test.php")
    fun setTestData(@HeaderMap headers: Map<String, String>, @Body testData: GetConfigRequest): Call<GetConfigResponse>
}