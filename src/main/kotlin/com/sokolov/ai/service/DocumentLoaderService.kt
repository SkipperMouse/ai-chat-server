package com.sokolov.ai.service

import com.sokolov.ai.config.IngestProperties
import com.sokolov.ai.domain.document.ProcessedDocument
import com.sokolov.ai.repository.DocumentRepository
import com.sokolov.ai.utils.UNKNOWN
import com.sokolov.ai.utils.contentHash
import org.slf4j.LoggerFactory
import org.springframework.ai.reader.TextReader
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.boot.CommandLineRunner
import org.springframework.core.io.Resource
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.stereotype.Service

@Service
class DocumentLoaderService(
    private val documentRepository: DocumentRepository,
    private val resolver: ResourcePatternResolver,
    private val vectorStore: VectorStore,
    private val ingestProperties: IngestProperties
) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(DocumentLoaderService::class.java)

    fun loadDocuments() {
        resolver.getResources("classpath:/knowledgebase/**/")
            .asSequence()
            .mapNotNull { res ->
                val filename = res.filename ?: return@mapNotNull null
                val ext = filename.substringAfterLast(".", "").lowercase()
                if (ext.isBlank()) return@mapNotNull null
                if (!ingestProperties.isExtAllowed(ext)) return@mapNotNull null
                Triple(res, res.contentHash(), ext)
            }
            .filter { (resource, hash, _) ->
                !documentRepository.existsByFilenameAndContentHash(resource.filename ?: UNKNOWN, hash)
            }
            .forEach { (resource, hash, type) -> loadDocument(resource, hash, type) }
    }

    private fun loadDocument(resource: Resource, hash: String, documentType: String) {
        log.debug("loadDocument: name=${resource.filename}")
        val documents = TextReader(resource).get()
        val textSplitter = TokenTextSplitter.builder()
            .withChunkSize(250)
            .build()
        val chunks = textSplitter.apply(documents)
        vectorStore.accept(chunks)

        val processedDocument = ProcessedDocument(
            filename = resource.filename ?: UNKNOWN,
            contentHash = hash,
            documentType = documentType,
            chunkCount = chunks.size
        )
        documentRepository.save(processedDocument)

    }

    override fun run(vararg args: String) {
        loadDocuments()
    }

}