package pey.vpn.net.api.api



import okhttp3.OkHttpClient
import pey.vpn.net.api.util.Constants
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitInstance {

    companion object {

        val timeoutInSeconds = 30L // Set your desired timeout value

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(timeoutInSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutInSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutInSeconds, TimeUnit.SECONDS)
            .build()

        fun retrofitClient(): SimpleApi {
            return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build().create(SimpleApi::class.java)
        }


    }

}
