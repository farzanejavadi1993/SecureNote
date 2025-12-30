package com.farzane.securenote

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform