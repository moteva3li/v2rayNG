package pey.vpn.net.fmt

import pey.vpn.net.dto.EConfigType
import pey.vpn.net.dto.ProfileItem
import pey.vpn.net.dto.V2rayConfig
import pey.vpn.net.util.JsonUtil

object CustomFmt : FmtBase() {
    fun parse(str: String): ProfileItem? {
        val config = ProfileItem.create(EConfigType.CUSTOM)

        val fullConfig = JsonUtil.fromJson(str, V2rayConfig::class.java)
        val outbound = fullConfig.getProxyOutbound()

        config.remarks = fullConfig?.remarks ?: System.currentTimeMillis().toString()
        config.server = outbound?.getServerAddress()
        config.serverPort = outbound?.getServerPort().toString()

        return config
    }
}