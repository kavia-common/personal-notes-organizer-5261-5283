package org.example.app.data.model

/**
 * PUBLIC_INTERFACE
 * Represents a note item with metadata and relations.
 */
data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val updatedAt: Long,
    val folderId: Long?,
    val pinned: Boolean,
    val favorite: Boolean
)

/**
 * PUBLIC_INTERFACE
 * Represents a folder used to categorize notes.
 */
data class Folder(
    val id: Long,
    val name: String
)
