package com.sokolov.ai.repository

import com.sokolov.ai.domain.document.LoadedDocument
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DocumentRepository: JpaRepository<LoadedDocument, Long>{
    fun existsByFilenameAndContentHash(filename: String, contentHash: String): Boolean
}