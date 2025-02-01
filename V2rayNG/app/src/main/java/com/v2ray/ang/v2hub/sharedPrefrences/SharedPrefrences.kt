package com.v2ray.ang.v2hub.sharedPrefrences

import android.content.Context
import com.v2ray.ang.v2hub.utils.Consts.Companion.SHARED_PREF_NAME
import com.v2ray.ang.v2hub.utils.Consts.Companion.SORTED_BY_FILTER

class SharedPrefrences {

    private fun getSharedPreferences(context: Context) = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

    fun getSortedByFilter(context: Context): String? {
        return getSharedPreferences(context).getString(SORTED_BY_FILTER, "Time")
    }

    fun setSortedByFilter(context: Context, filter : String) {
        getSharedPreferences(context).edit().putString(SORTED_BY_FILTER, filter).apply()
    }

}