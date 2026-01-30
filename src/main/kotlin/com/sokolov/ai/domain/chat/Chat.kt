package com.sokolov.ai.domain.chat

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
open class Chat(
    @Id
    @GeneratedValue(GenerationType.IDENTITY)
    var id: Long? = null,
    val title: String,
    @CreationTimestamp
    var createdAt: Instant? = null,
    @OrderBy("createdAt ASC")
    @OneToMany(
        fetch = FetchType.EAGER,
        orphanRemoval = true,
        cascade = [CascadeType.ALL],
        mappedBy = "chat"
    )
    var messageHistory: MutableList<ChatMessage> = mutableListOf()
) {

    fun addAllMessages(chatEntries: List<ChatMessage>) {
        messageHistory.addAll(chatEntries)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Chat) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Chat(id=$id)"
    }
}
