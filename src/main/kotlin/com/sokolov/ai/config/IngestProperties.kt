package com.sokolov.ai.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ai.ingest")
data class IngestProperties(
    val allowedExtensions: Set<String> = emptySet()
) {
    fun isExtAllowed(ext: String): Boolean {
        return ext in allowedExtensions
    }
}
