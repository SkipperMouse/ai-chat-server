package com.sokolov.ai.domain.document

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
class LoadedDocument(
    @Id
    @GeneratedValue(GenerationType.IDENTITY)
    var id: Long?,
    val filename: String,
    val contentHash: String,
    val documentType: String,
    val chunkCount: Int,
    @CreationTimestamp
    var loadedAt: Instant?
)