package com.sokolov.ai.infrastructure.mapper

import com.sokolov.ai.domain.chat.ChatMessage
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.MessageType
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage

fun Message.toChatMessage(chatId: Long): ChatMessage = ChatMessage(
    content = text,
    role = messageType,
    chatId = chatId
)

fun ChatMessage.toMessage(): Message =
    when (role) {
        MessageType.USER -> UserMessage(content)
        MessageType.ASSISTANT -> AssistantMessage(content)
        MessageType.SYSTEM -> SystemMessage(content)
        else -> {
            throw UnsupportedOperationException("tool message type is not supported yet")
        }
    }
