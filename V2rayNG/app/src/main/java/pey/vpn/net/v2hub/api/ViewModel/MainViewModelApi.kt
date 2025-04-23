package pey.vpn.net.api.ViewModel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pey.vpn.net.api.Model.ConfigsRequestBody
import pey.vpn.net.api.Model.ResponseData
import pey.vpn.net.api.repository.Repository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainViewModelApi() : ViewModel() {

    val repository: Repository = Repository()

    fun postLogin(rawBody: ConfigsRequestBody): LiveData<Response<ResponseData>> {
        val myRepository: MutableLiveData<Response<ResponseData>> = MutableLiveData()
        val response = repository.postLogin(rawBody)

        response.enqueue(object : Callback<ResponseData> {
            override fun onResponse(
                call: Call<ResponseData>,
                response: Response<ResponseData>
            ) {
                myRepository.value = response
            }

            override fun onFailure(call: Call<ResponseData?>, t: Throwable) {
                Log.i("MainViewModel", "onFailure: $t")
            }
        })

        return myRepository
    }



}


