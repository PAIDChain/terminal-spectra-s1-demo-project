package my.paidchain.spectraterminal.common.network.rest

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit


object RetrofitHelper {
    private var mOkHttpClient: OkHttpClient? = null

    private var CONNECTION_TIMEOUT: Long = 30
    private var READ_TIMEOUT: Long = 20
    private var WRITE_TIMEOUT: Long = 20

    fun <T> createApi(clazz: Class<T>, baseUrl: String, context: Context): T {


        if (mOkHttpClient == null)
            initOkHttpClient(context)
        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(mOkHttpClient!!)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()



        return retrofit.create(clazz)
    }

    private fun initOkHttpClient(context: Context) {
        val httpLoggingInterceptor = HttpLoggingInterceptor()

        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        if (mOkHttpClient == null) {
            synchronized(RetrofitHelper::class.java) {
                if (mOkHttpClient == null) {

                    mOkHttpClient = OkHttpClient.Builder()
                            //.cache(cache)
                            //.addInterceptor(SpectraSignInterceptor())
                            .addInterceptor(NetworkConnectionInterceptor(context))
                            .addInterceptor(httpLoggingInterceptor)
                            .retryOnConnectionFailure(true)
                            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                            .build()


                }

            }
        }
    }




}



class NoConnectivityException : IOException() {

    // You can send any message whatever you want from here.
    override val message: String
        get() = "No Internet Connection"
}

class NetworkConnectionInterceptor(private val mContext: Context) : Interceptor {

    private val isConnected: Boolean
        get() {
            val connectivityManager = mContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = connectivityManager.activeNetworkInfo
            return netInfo != null && netInfo.isConnected
        }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isConnected) {
            throw NoConnectivityException()
            // Throwing our custom exception 'NoConnectivityException'
        }

        val builder = chain.request().newBuilder()
        return chain.proceed(builder.build())
    }

}