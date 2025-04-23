package pey.vpn.net.api.repository



import pey.vpn.net.api.Model.ConfigsRequestBody
import pey.vpn.net.api.Model.ResponseData
import pey.vpn.net.api.api.RetrofitInstance
import retrofit2.Call



class Repository {

    fun postLogin(loginRequest: ConfigsRequestBody): Call<ResponseData> {
        return RetrofitInstance.retrofitClient().postLogin(loginRequest)
    }

}


