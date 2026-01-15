package com.sokolov.ai.repository

import com.sokolov.ai.domain.chat.Chat
import com.sokolov.ai.infrastructure.mapper.toChatMessage
import com.sokolov.ai.infrastructure.mapper.toMessage
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.memory.ChatMemoryRepository
import org.springframework.ai.chat.messages.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface ChatRepository : JpaRepository<Chat, Long>, ChatMemoryRepository {
    @Query("select cast(c.id as string) from Chat c")
    override fun findConversationIds(): List<String>

    override fun findByConversationId(conversationId: String): List<Message> {
        if (conversationId == ChatMemory.DEFAULT_CONVERSATION_ID) {
            return emptyList()
        }
        return getChat(conversationId)
            .messageHistory
            .map { it.toMessage() }
    }

    override fun saveAll(conversationId: String, messages: List<Message>) {
        if (conversationId == ChatMemory.DEFAULT_CONVERSATION_ID) {
            return
        }
        val chat = getChat(conversationId)
        chat.messageHistory
            .addAll(messages.map { it.toChatMessage(chat) })
        save(chat)
    }

    override fun deleteByConversationId(conversationId: String) {
        throw UnsupportedOperationException("Use repository methods to manage chat history")
    }

    private fun ChatRepository.getChat(conversationId: String): Chat {
        return findById(conversationId.toLong()).orElseThrow()
    }
}
