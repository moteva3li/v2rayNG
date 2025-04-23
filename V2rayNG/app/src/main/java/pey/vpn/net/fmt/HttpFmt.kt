package pey.vpn.net.fmt

import pey.vpn.net.dto.EConfigType
import pey.vpn.net.dto.ProfileItem
import pey.vpn.net.dto.V2rayConfig.OutboundBean
import pey.vpn.net.extension.isNotNullEmpty
import kotlin.text.orEmpty

object HttpFmt : FmtBase() {
    fun toOutbound(profileItem: ProfileItem): OutboundBean? {
        val outboundBean = OutboundBean.create(EConfigType.HTTP)

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