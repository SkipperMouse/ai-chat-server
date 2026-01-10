package com.sokolov.ai.domain.chat

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.springframework.ai.chat.messages.MessageType
import java.time.Instant

@Entity
@Table(name = "chat_message")
class ChatMessage(
    @Id
    @GeneratedValue(GenerationType.IDENTITY)
    var id: Long? = null,
    var content: String,
    @Enumerated(EnumType.STRING)
    var role: MessageType,
    @Column(name = "chat_id", nullable = false)
    val chatId: Long,
    @CreationTimestamp
    var createdAt: Instant? = null

)

