package com.sokolov.ai.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("ai.rag-advisor")
data class RagAdviserProperties(
    val topK: Int,
    val similarityThreshold: Double

)
