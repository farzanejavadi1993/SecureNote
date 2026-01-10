package com.farzane.securenote.domain.manager

interface AuthManager {
    fun hasPin(): Boolean
    fun savePin(pin: String)
    fun validatePin(inputPin: String): Boolean
    fun removePin()
}
