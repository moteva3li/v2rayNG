package pey.vpn.net.v2hub.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pey.vpn.net.api.Model.Config
import pey.vpn.net.api.Model.ResponseData

class ConfigManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ConfigPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val CONFIG_KEY = "configs"

    // Save configs to SharedPreferences
    fun saveConfigs(configResponse: ResponseData) {
        val editor = prefs.edit()
        val json = gson.toJson(configResponse)
        editor.putString(CONFIG_KEY, json)
        editor.apply()
    }

    // Get saved configs
    fun getConfigs(): ResponseData? {
        val json = prefs.getString(CONFIG_KEY, null) ?: return null
        val type = object : TypeToken<ResponseData>() {}.type
        return gson.fromJson(json, type)
    }

    // Clear configs
    fun clearConfigs() {
        prefs.edit().remove(CONFIG_KEY).apply()
    }

    // Update configs with new response
    fun updateConfigs(newConfigResponse: ResponseData) {
        val currentConfigs = getConfigs()

        // If new configs are empty, clear SharedPreferences
        if (newConfigResponse.configs.isEmpty()) {
            clearConfigs()
            return
        }

        // If no existing configs, save new ones
        if (currentConfigs == null) {
            saveConfigs(newConfigResponse)
            return
        }

        // Compare and update if different
        if (currentConfigs.configs != newConfigResponse.configs) {
            saveConfigs(newConfigResponse)
        }
    }

    // Get config list
    fun getConfigList(): List<Config> {
        return getConfigs()?.configs ?: emptyList()
    }
}