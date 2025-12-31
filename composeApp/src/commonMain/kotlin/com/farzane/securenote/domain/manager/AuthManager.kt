package com.farzane.securenote.domain.manager

/**
 * Manages the app's authentication state, including the PIN lock and auto-lock timer.
 * This class is the single source of truth for knowing if the app should be locked.
 */
class AuthManager(private val storage: KeyValueStorage) {

    private val KEY_PIN = "user_pin"

    // Tracks the last time the user interacted with the app.
    private var lastActiveTime: Long = currentTimeMillis()


    /**
     * The current lock state of the app.
     * `true` if the user needs to enter a PIN, `false` otherwise.
     * It's private so only this class can change it.
     */
    var isAppLocked: Boolean = true
        private set

    init {
        // When the app starts, it should be locked if a PIN has already been set.
        // If no PIN exists, it's technically "unlocked" for the setup process.
        isAppLocked = hasPin()
    }

    /**
     * Checks if the user has set up a PIN.
     * @return `true` if a PIN is stored, `false` otherwise.
     */
    fun hasPin(): Boolean {
        return storage.getPin() != null
    }

    /**
     * Saves a new PIN to storage after the user creates it.
     * The app is immediately unlocked after setting the PIN.
     */
    fun savePin(pin: String) {
        storage.savePin(pin)
        isAppLocked = false // Unlock immediately after setting
        updateActivity() // Reset the timer right after setup.
    }

    /**
     * Compares the user's input with the stored PIN.
     * @return `true` if the PIN is correct, `false` otherwise.
     */
    fun validatePin(inputPin: String): Boolean {
        val storedPin = storage.getPin()
        if (inputPin == storedPin) {
            isAppLocked = false
            updateActivity() // Reset the timer after a successful login.
            return true
        }
        return false
    }

    /**
     * Resets the inactivity timer.
     * This should be called on any user interaction to keep the app unlocked.
     */
    fun updateActivity() {
        lastActiveTime = currentTimeMillis()
    }

    /**
     * This is typically called when the app resumes from the background.
     */
    fun checkShouldLock() {
        if (hasPin() ) {
            isAppLocked = true
        }
    }

    /**
     * Manually locks the app immediately.
     * Used for the "Lock" button in the UI.
     */
    fun lockApp() {
        isAppLocked = true
    }

    private fun currentTimeMillis(): Long = System.currentTimeMillis()
}
