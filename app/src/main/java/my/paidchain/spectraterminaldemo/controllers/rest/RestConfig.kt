package my.paidchain.spectraterminaldemo.controllers.rest

import android.content.Context
import my.paidchain.spectraterminaldemo.common.network.rest.RetrofitHelper
import my.paidchain.spectraterminaldemo.models.rest.AresAPI
import my.paidchain.spectraterminaldemo.models.rest.GetConfigRequest
import my.paidchain.spectraterminaldemo.models.rest.GetConfigResponse
import my.paidchain.spectraterminaldemo.views.rest.IRestConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference

class RestConfig() {
    private var restConfig = WeakReference<IRestConfig>(null)
    fun updateConfig(_context: Context){

        val baseUrl: String = "https://api-uat.paidchain.my"//"https://www.googleapis.com"//
        val sToken: String = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczpcL1wvd3d3LmRjdWJlei5jb20iLCJhdWQiOiJodHRwczpcL1wvd3d3LmRjdWJlei5jb20iLCJpYXQiOjE2Nzg4NjY2NzUsIm5iZiI6MTY3ODg2NjY3NSwiZXhwIjoxNjc5NzMwNjc1LCJkYXRhIjp7ImlkIjoiMSIsImZ1bGxfbmFtZSI6IlRoaWFnYSIsImVtYWlsIjoiYWRtaW5AZGN1YmV6LmNvbSIsIm1vYmlsZSI6IjYwMTIyOTU3NDcwIiwidXBkYXRlcyI6IjAiLCJsZXZlbCI6IjEifX0.tjff5qTXPwe8NqY2vWvTu6Fp-7UHUqNNMmQTeN7HVDA"
        val headers = mapOf("Authorization" to "Bearer $sToken", "Content-Type" to "application/json")
        val sValue = "1"

        RetrofitHelper.createApi(AresAPI::class.java, baseUrl, _context)
            .setTestData(headers, GetConfigRequest(sValue))
            .enqueue(object : Callback<GetConfigResponse> {

                override fun onResponse(call: Call<GetConfigResponse>, response: Response<GetConfigResponse>) {
                    if(response.isSuccessful){
                        val msg = response.body() as GetConfigResponse

                        restConfig.get()?.onConfigUpdateSuccess(msg.error, msg.message)

                    }else {
                        restConfig.get()?.onConfigUpdateFail(102, "Failed Call")
                    }
                }

                override fun onFailure(call: Call<GetConfigResponse>, t: Throwable) {
                    restConfig.get()?.onConfigUpdateFail(101, t.message)
                }
            })
    }
    fun addListener(restConfig: IRestConfig){
        this.restConfig = WeakReference(restConfig)
    }
}