package com.tigerworkshop.sms2telegram.data

import android.content.Context
import androidx.core.content.edit
import com.kashif.otprelay.BuildConfig

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Credentials are baked in at build time - this is a zero-config build. */
    fun loadSettings(): TelegramSettings? {
        val token = BuildConfig.BOT_TOKEN.takeIf { it.isNotBlank() }
        val chatId = BuildConfig.CHAT_ID.takeIf { it.isNotBlank() }
        return if (token != null && chatId != null) {
            TelegramSettings(token, chatId)
        } else {
            null
        }
    }

    fun saveDeviceInfo(name: String, phone: String) {
        prefs.edit {
            putString(KEY_DEVICE_NAME, name.trim())
            putString(KEY_DEVICE_PHONE, phone.trim())
        }
    }

    fun clearDeviceInfo() {
        prefs.edit {
            remove(KEY_DEVICE_NAME)
            remove(KEY_DEVICE_PHONE)
        }
    }

    fun getDeviceName(): String? = prefs.getString(KEY_DEVICE_NAME, null)?.takeIf { it.isNotBlank() }

    fun getDevicePhone(): String? = prefs.getString(KEY_DEVICE_PHONE, null)?.takeIf { it.isNotBlank() }

    fun hasDeviceInfo(): Boolean = getDeviceName() != null && getDevicePhone() != null

    fun isFirstLaunch(): Boolean = prefs.getBoolean(KEY_FIRST_LAUNCH, true)

    fun setFirstLaunch(isFirstLaunch: Boolean) {
        prefs.edit {
            putBoolean(KEY_FIRST_LAUNCH, isFirstLaunch)
        }
    }

    fun saveLastForwardStatus(status: String) {
        prefs.edit {
            putString(KEY_LAST_STATUS, status)
        }
    }

    fun loadLastForwardStatus(): String? = prefs.getString(KEY_LAST_STATUS, null)

    fun isForwardingEnabled(): Boolean = prefs.getBoolean(KEY_FORWARDING_ENABLED, true)

    fun setForwardingEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_FORWARDING_ENABLED, enabled)
        }
    }

    fun isShowSimNameEnabled(): Boolean = prefs.getBoolean(KEY_SHOW_SIM_NAME, false)

    fun setShowSimNameEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_SHOW_SIM_NAME, enabled)
        }
    }

    data class TelegramSettings(
        val apiToken: String,
        val chatId: String
    )

    companion object {
        private const val PREFS_NAME = "sms_forwarder_prefs"
        private const val KEY_DEVICE_NAME = "device_name"
        private const val KEY_DEVICE_PHONE = "device_phone"
        private const val KEY_LAST_STATUS = "last_forward_status"
        private const val KEY_FORWARDING_ENABLED = "forwarding_enabled"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_SHOW_SIM_NAME = "show_sim_name"
    }
}
