package pey.vpn.net.api.Model



data class ResponseData(
    val app_data: AppData,
    val configs: List<Config>
)

data class AppData(
    val version_android: String,
    val force_update_android: Boolean
)

data class Config(
    val config: String,
    val country: String,
    val flag: String
)

data class ConfigsRequestBody(
    val uuid: String,
    val password: String
)






