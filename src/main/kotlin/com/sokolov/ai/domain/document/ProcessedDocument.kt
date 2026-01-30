package com.sokolov.ai.domain.document

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import javax.validation.constraints.Size

@Entity
open class ProcessedDocument(
    @Id
    @GeneratedValue(GenerationType.IDENTITY)
    var id: Long? = null,
    val filename: String,
    val contentHash: String,
    @Size(min = 1, max = 10)
    val documentType: String,
    val chunkCount: Int,
    @CreationTimestamp
    var createdAt: Instant? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProcessedDocument) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "ProcessedDocument(id=$id)"
    }
}