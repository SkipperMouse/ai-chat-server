package com.sokolov.ai.domain.chat

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
@Table(name = "chat_entry")
class ChatEntry(
    @Id
    @GeneratedValue(GenerationType.IDENTITY)
    var id: Long? = null,
    var content: String,
    @Enumerated(EnumType.STRING)
    var role: MessageType,
    @CreationTimestamp
    var createdAt: Instant? = null

)

