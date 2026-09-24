package com.example.data.repository

import com.example.data.local.dao.ChatDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface ChatRepository {
    fun getAllConversations(): Flow<List<ConversationEntity>>
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessageEntity>>
    suspend fun createNewConversation(title: String = "নতুন চ্যাট", language: String = "bn"): String
    suspend fun saveMessage(
        conversationId: String,
        sender: String,
        text: String,
        language: String = "bn",
        imageUri: String? = null
    ): Long
    suspend fun updateMessage(message: ChatMessageEntity)
    suspend fun deleteMessage(messageId: Long)
    suspend fun updateConversationTitle(id: String, title: String)
    suspend fun deleteConversation(id: String)
    suspend fun clearAllHistory()
}

class ChatRepositoryImpl(private val chatDao: ChatDao) : ChatRepository {
    override fun getAllConversations(): Flow<List<ConversationEntity>> =
        chatDao.getAllConversations()

    override fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForConversation(conversationId)

    override suspend fun createNewConversation(title: String, language: String): String {
        val id = UUID.randomUUID().toString()
        val conv = ConversationEntity(
            id = id,
            title = title,
            updatedAt = System.currentTimeMillis(),
            previewText = "",
            language = language
        )
        chatDao.insertConversation(conv)
        return id
    }

    override suspend fun saveMessage(
        conversationId: String,
        sender: String,
        text: String,
        language: String,
        imageUri: String?
    ): Long {
        val msg = ChatMessageEntity(
            conversationId = conversationId,
            sender = sender,
            text = text,
            timestamp = System.currentTimeMillis(),
            language = language,
            imageUri = imageUri
        )
        val id = chatDao.insertMessage(msg)
        val preview = if (text.length > 60) text.take(60) + "..." else text
        chatDao.updateConversationPreview(conversationId, preview, System.currentTimeMillis())
        return id
    }

    override suspend fun updateMessage(message: ChatMessageEntity) {
        chatDao.updateMessage(message)
    }

    override suspend fun deleteMessage(messageId: Long) {
        chatDao.deleteMessageById(messageId)
    }

    override suspend fun updateConversationTitle(id: String, title: String) {
        chatDao.updateConversationTitle(id, title)
    }

    override suspend fun deleteConversation(id: String) {
        chatDao.deleteMessagesByConversationId(id)
        chatDao.deleteConversationById(id)
    }

    override suspend fun clearAllHistory() {
        chatDao.clearAllMessages()
        chatDao.clearAllConversations()
    }
}
