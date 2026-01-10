package com.sokolov.ai.domain.document

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import javax.validation.constraints.Size

@Entity
class ProcessedDocument(
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
)