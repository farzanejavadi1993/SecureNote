package com.farzane.securenote.data.mapper

import com.farzane.securenote.data.local.entity.NoteEntity
import com.farzane.securenote.domain.model.Note
import kotlin.test.Test
import kotlin.test.assertEquals

class NoteMapperTest {

    @Test
    fun `converts Entity to Domain model correctly`() {
        // Arrange
        val entity = NoteEntity(
            id = 1,
            title = "My Title",
            content = "My Content",
            timestamp = 123456789L
        )

        // Act
        val domainNote = entity.toDomain()

        // Assert
        assertEquals(1, domainNote.id)
        assertEquals("My Title", domainNote.title)
        assertEquals("My Content", domainNote.content)
        assertEquals(123456789L, domainNote.timestamp)
    }

    @Test
    fun `converts Domain model to Entity correctly`() {
        // Arrange
        val domainNote = Note(
            id = 55,
            title = "Work",
            content = "Meeting",
            timestamp = 987654321L
        )

        // Act
        val entity = domainNote.toEntity()

        // Assert
        assertEquals(55, entity.id)
        assertEquals("Work", entity.title)
        assertEquals(987654321L, entity.timestamp)
    }

    @Test
    fun `handles null ID in Domain model by defaulting to 0 for Entity`() {
        // Arrange: A new note usually has a null ID
        val newNote = Note(
            id = null,
            title = "New Note",
            content = "Text",
            timestamp = 100L
        )

        // Act
        val entity = newNote.toEntity()

        // Assert
        // Room expects 0 for auto-generating new IDs
        assertEquals(0, entity.id)
    }
}
