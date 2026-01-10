package com.farzane.securenote.domain.manager

/**
 * Manages the app's authentication state, including the PIN lock and auto-lock timer.
 * This class is the single source of truth for knowing if the app should be locked.
 */
class AuthManagerImpl(private val storage: KeyValueStorage) : AuthManager {

    /**
     * Checks if the user has set up a PIN.
     * @return `true` if a PIN is stored, `false` otherwise.
     */
    override fun hasPin(): Boolean {
        return storage.getPin() != null
    }

    /**
     * Saves a new PIN to storage after the user creates it.
     * The app is immediately unlocked after setting the PIN.
     */
    override fun savePin(pin: String) {
        storage.savePin(pin)
    }

    /**
     * Compares the user's input with the stored PIN.
     * @return `true` if the PIN is correct, `false` otherwise.
     */
   override fun validatePin(inputPin: String): Boolean {
        val storedPin = storage.getPin()
        return inputPin == storedPin
    }

    
    /**
     * Removes the user's PIN from storage, effectively disabling the lock.
     */
   override fun removePin() {
        storage.clearPin() // We'll create this function in the interface next.
    }
}
