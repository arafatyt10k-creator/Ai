package com.example.data.repository

import com.example.data.local.dao.MemoryDao
import com.example.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

interface MemoryRepository {
    fun getAllMemories(): Flow<List<MemoryEntity>>
    suspend fun addMemory(category: String, fact: String): Long
    suspend fun deleteMemory(id: Long)
    suspend fun clearAllMemories()
}

class MemoryRepositoryImpl(private val memoryDao: MemoryDao) : MemoryRepository {
    override fun getAllMemories(): Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    override suspend fun addMemory(category: String, fact: String): Long {
        val memory = MemoryEntity(
            category = category,
            fact = fact,
            createdAt = System.currentTimeMillis()
        )
        return memoryDao.insertMemory(memory)
    }

    override suspend fun deleteMemory(id: Long) {
        memoryDao.deleteMemoryById(id)
    }

    override suspend fun clearAllMemories() {
        memoryDao.clearAllMemories()
    }
}
