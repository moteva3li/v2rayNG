package pey.vpn.net.fmt

import pey.vpn.net.dto.EConfigType
import pey.vpn.net.dto.ProfileItem
import pey.vpn.net.dto.V2rayConfig.OutboundBean
import pey.vpn.net.extension.idnHost
import pey.vpn.net.extension.isNotNullEmpty
import pey.vpn.net.util.Utils
import java.net.URI
import kotlin.text.orEmpty

object SocksFmt : FmtBase() {
    fun parse(str: String): ProfileItem? {
        val config = ProfileItem.create(EConfigType.SOCKS)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.idnHost.isEmpty()) return null
        if (uri.port <= 0) return null

        config.remarks = Utils.urlDecode(uri.fragment.orEmpty())
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()

        if (uri.userInfo?.isEmpty() == false) {
            val result = Utils.decode(uri.userInfo).split(":", limit = 2)
            if (result.count() == 2) {
                config.username = result.first()
                config.password = result.last()
            }
        }

        return config
    }

    fun toUri(config: ProfileItem): String {
        val pw =
            if (config.username.isNotNullEmpty())
                "${config.username}:${config.password}"
            else
                ":"

        return toUri(config, Utils.encode(pw), null)
    }

    fun toOutbound(profileItem: ProfileItem): OutboundBean? {
        val outboundBean = OutboundBean.create(EConfigType.SOCKS)

        outboundBean?.settings?.servers?.first()?.let { server ->
            server.address = profileItem.server.orEmpty()
            server.port = profileItem.serverPort.orEmpty().toInt()
            if (profileItem.username.isNotNullEmpty()) {
                val socksUsersBean = OutboundBean.OutSettingsBean.ServersBean.SocksUsersBean()
                socksUsersBean.user = profileItem.username.orEmpty()
                socksUsersBean.pass = profileItem.password.orEmpty()
                server.users = listOf(socksUsersBean)
            }
        }

        return outboundBean
    }

}