package com.farzane.securenote.domain.model

data class Note(
    val id: Long? = null, // Nullable for new notes
    val title: String,
    val content: String,
    val timestamp: Long
)
