package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Partner

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("milan_app_prefs", Context.MODE_PRIVATE)

    var currentPartnerId: String
        get() = prefs.getString(KEY_CURRENT_PARTNER, Partner.MALTA.id) ?: Partner.MALTA.id
        set(value) = prefs.edit().putString(KEY_CURRENT_PARTNER, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var customMaltaName: String
        get() = prefs.getString(KEY_MALTA_NAME, "Anish") ?: "Anish"
        set(value) = prefs.edit().putString(KEY_MALTA_NAME, value).apply()

    var customNepalName: String
        get() = prefs.getString(KEY_NEPAL_NAME, "Puri") ?: "Puri"
        set(value) = prefs.edit().putString(KEY_NEPAL_NAME, value).apply()

    var fcmToken: String?
        get() = prefs.getString(KEY_FCM_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_FCM_TOKEN, value).apply()

    var lastNotifiedPingTime: Long
        get() = prefs.getLong(KEY_LAST_NOTIFIED_PING_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_NOTIFIED_PING_TIME, value).apply()

    var isKeepAliveEnabled: Boolean
        get() = prefs.getBoolean(KEY_KEEP_ALIVE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_KEEP_ALIVE_ENABLED, value).apply()

    companion object {
        private const val KEY_CURRENT_PARTNER = "current_partner"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_MALTA_NAME = "custom_malta_name"
        private const val KEY_NEPAL_NAME = "custom_nepal_name"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_LAST_NOTIFIED_PING_TIME = "last_notified_ping_time"
        private const val KEY_KEEP_ALIVE_ENABLED = "keep_alive_enabled"
    }
}
