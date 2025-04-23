package pey.vpn.net.v2hub.sharedPrefrences

import android.content.Context
import pey.vpn.net.v2hub.utils.Consts.Companion.NOTIFICATION
import pey.vpn.net.v2hub.utils.Consts.Companion.SHARED_PREF_NAME
import pey.vpn.net.v2hub.utils.Consts.Companion.SORTED_BY_FILTER
import pey.vpn.net.v2hub.utils.Consts.Companion.SPEED_STATUS

class SharedPrefrences {

    private fun getSharedPreferences(context: Context) = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

    fun getSortedByFilter(context: Context): String? {
        return getSharedPreferences(context).getString(SORTED_BY_FILTER, "Time")
    }

    fun setSortedByFilter(context: Context, filter : String) {
        getSharedPreferences(context).edit().putString(SORTED_BY_FILTER, filter).apply()
    }

    fun getSpeedStatus(context: Context): Boolean? {
        return getSharedPreferences(context).getBoolean(SPEED_STATUS, false)
    }

    fun setSpeedStatus(context: Context, bool : Boolean) {
        getSharedPreferences(context).edit().putBoolean(SPEED_STATUS, bool).apply()
    }

    fun getNotificationStatus(context: Context): Boolean? {
        return getSharedPreferences(context).getBoolean(NOTIFICATION, false)
    }

    fun setNotificationStatus(context: Context, bool : Boolean) {
        getSharedPreferences(context).edit().putBoolean(NOTIFICATION, bool).apply()
    }

}