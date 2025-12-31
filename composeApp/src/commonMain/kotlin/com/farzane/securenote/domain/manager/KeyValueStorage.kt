package com.farzane.securenote.domain.manager

interface KeyValueStorage {
    fun savePin(pin: String)
    fun getPin(): String?
}
