package com.farzane.securenote.domain.manager

import java.io.File
import java.util.Properties

class DesktopKeyValueStorage : KeyValueStorage {
    // We use a simple properties file to store the PIN on Desktop
    private val file = File("app_settings.properties")
    private val props = Properties()

    init {
        // Load existing settings if the file exists
        if (file.exists()) {
            file.inputStream().use { props.load(it) }
        }
    }

    override fun savePin(pin: String) {
        props.setProperty("user_pin", pin)
        // Write changes to file immediately
        file.outputStream().use { props.store(it, null) }
    }

    override fun getPin(): String? {
        return props.getProperty("user_pin")
    }

    override fun clearPin() {
        props.remove("user_pin")
        file.outputStream().use { props.store(it, null) }
    }
}