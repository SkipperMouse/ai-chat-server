package com.sokolov.ai.domain.chat

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.JoinColumn
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
class Chat(
    @Id
    @GeneratedValue(GenerationType.IDENTITY)
    var id: Long? = null,
    val title: String,
    @CreationTimestamp
    var createdAt: Instant? = null,
    @OneToMany(
        fetch = FetchType.EAGER,
        orphanRemoval = true,
        cascade = [CascadeType.ALL]
    )
    @JoinColumn(name = "chat_id")
    val history: MutableList<ChatEntry> = mutableListOf()
) {
    fun addEntry(chatEntry: ChatEntry) {
        history.add(chatEntry)
    }

    fun addAllEntries(chatEntries: List<ChatEntry>) {
        history.addAll(chatEntries)
    }
}
