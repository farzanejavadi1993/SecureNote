package com.farzane.securenote.data.mapper

import com.farzane.securenote.data.local.entity.NoteEntity
import com.farzane.securenote.domain.model.Note

fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        timestamp = timestamp
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id ?: 0,
        title = title,
        content = content,
        timestamp = timestamp
    )
}