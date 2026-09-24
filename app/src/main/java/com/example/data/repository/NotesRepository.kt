package com.example.data.repository

import com.example.data.local.dao.NoteDao
import com.example.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun getAllNotes(): Flow<List<NoteEntity>>
    fun searchNotes(query: String): Flow<List<NoteEntity>>
    fun getNotesByCategory(category: String): Flow<List<NoteEntity>>
    fun getAllCategories(): Flow<List<String>>
    suspend fun addNote(title: String, content: String, category: String = "General", colorTag: Long = 0xFF00F0FF): Long
    suspend fun updateNote(note: NoteEntity)
    suspend fun deleteNote(id: Long)
    suspend fun togglePin(id: Long)
    suspend fun toggleFavorite(id: Long)
}

class NotesRepositoryImpl(private val noteDao: NoteDao) : NotesRepository {
    override fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    override fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    override fun getNotesByCategory(category: String): Flow<List<NoteEntity>> = noteDao.getNotesByCategory(category)

    override fun getAllCategories(): Flow<List<String>> = noteDao.getAllCategories()

    override suspend fun addNote(title: String, content: String, category: String, colorTag: Long): Long {
        val note = NoteEntity(
            title = title.ifBlank { "Untitled Note" },
            content = content,
            category = category.ifBlank { "General" },
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            colorTag = colorTag
        )
        return noteDao.insertNote(note)
    }

    override suspend fun updateNote(note: NoteEntity) {
        noteDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteNote(id: Long) {
        noteDao.deleteNoteById(id)
    }

    override suspend fun togglePin(id: Long) {
        noteDao.togglePin(id)
    }

    override suspend fun toggleFavorite(id: Long) {
        noteDao.toggleFavorite(id)
    }
}
