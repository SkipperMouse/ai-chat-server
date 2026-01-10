package com.sokolov.ai.infrastructure.mapper

import com.sokolov.ai.domain.chat.ChatEntry
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.MessageType
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage

fun Message.toChatEntry(chatId: Long): ChatEntry = ChatEntry(
    content = text,
    role = messageType
)

fun ChatEntry.toMessage(): Message =
    when (role) {
        MessageType.USER -> UserMessage(content)
        MessageType.ASSISTANT -> AssistantMessage(content)
        MessageType.SYSTEM -> SystemMessage(content)
        else -> {
            throw UnsupportedOperationException("tool message type is not supported yet")
        }
    }
