package com.farzane.securenote.presentation

import com.farzane.securenote.domain.manager.AuthManager

/**
 * A Fake implementation of AuthManager for Unit Testing.
 * It stores the PIN in memory instead of persistent storage.
 */

class FakeAuthManager : AuthManager {

    // Simple in-memory variable to "fake" the storage
    private var fakeStoredPin: String? = null

    /**
     * Overridden to check our local variable instead of the real storage.
     */
    override fun hasPin(): Boolean {
        return fakeStoredPin != null
    }

    /**
     * Simulates saving the PIN to memory.
     */
    override fun savePin(pin: String) {
        fakeStoredPin = pin
    }

    /**
     * Validates input against our in-memory PIN.
     */
    override fun validatePin(inputPin: String): Boolean {
        return inputPin == fakeStoredPin
    }

    /**
     * Clears the in-memory PIN.
     */
    override fun removePin() {
        fakeStoredPin = null
    }


    fun setFakePin(pin: String) {
        fakeStoredPin = pin
    }
}
