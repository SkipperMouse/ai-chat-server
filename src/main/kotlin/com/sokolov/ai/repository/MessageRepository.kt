package com.sokolov.ai.repository

import com.sokolov.ai.domain.chat.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<ChatMessage, Long> {
}