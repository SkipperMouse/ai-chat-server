package com.sokolov.ai.infrastructure

import com.sokolov.ai.exception.NotFoundException
import com.sokolov.ai.infrastructure.mapper.toChatMessage
import com.sokolov.ai.infrastructure.mapper.toMessage
import com.sokolov.ai.repository.ChatRepository
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.messages.Message

open class PostgresChatMemory(val chatRepository: ChatRepository, val maxMessages: Int) : ChatMemory {

    override fun add(conversationId: String, messages: List<Message>) {
        val chatEntries = messages.map { it.toChatMessage(conversationId.toLong()) }
        val chat = chatRepository.findById(conversationId.toLong())
            .orElseThrow { NotFoundException(conversationId.toLong()) }
        chat.addAllMessages(chatEntries)
        chatRepository.save(chat)
    }

    override fun get(conversationId: String): List<Message> {
        val chat = chatRepository.findById(conversationId.toLong())
            .orElseThrow { NotFoundException(conversationId.toLong()) }
        return chat.messageHistory
            .takeLast(maxMessages)
            .map { it.toMessage() }
    }

    override fun clear(conversationId: String) {
        throw UnsupportedOperationException("Use repository methods to manage chat history")
    }
}

