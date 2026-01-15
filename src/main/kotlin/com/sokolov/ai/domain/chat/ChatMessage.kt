package com.sokolov.ai.domain.chat

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    var chat: Chat,
    @CreationTimestamp
    var createdAt: Instant? = null
)

