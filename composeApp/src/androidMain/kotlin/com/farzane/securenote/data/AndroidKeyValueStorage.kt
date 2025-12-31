package com.farzane.securenote.data

import android.content.Context
import com.farzane.securenote.domain.manager.KeyValueStorage
import androidx.core.content.edit

class AndroidKeyValueStorage(context: Context) : KeyValueStorage {
    // We use SharedPreferences which is standard on Android for simple key-value data
    private val prefs = context.getSharedPreferences(
        "secure_app_prefs",
        Context.MODE_PRIVATE
    )

    override fun savePin(pin: String) {
        prefs.edit { putString("user_pin", pin) }
    }

    override fun getPin(): String? {
        return prefs.getString("user_pin", null)
    }
}
