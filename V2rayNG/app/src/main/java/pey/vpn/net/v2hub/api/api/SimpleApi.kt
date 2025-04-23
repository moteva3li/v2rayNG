package pey.vpn.net.api.api

import pey.vpn.net.api.Model.ConfigsRequestBody
import pey.vpn.net.api.Model.ResponseData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface SimpleApi {

    @POST("users/singup_singin/")
    fun postLogin(
        @Body loginRequest: ConfigsRequestBody
    ): Call<ResponseData>

}
