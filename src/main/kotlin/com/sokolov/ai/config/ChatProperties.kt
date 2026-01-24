package com.sokolov.ai.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("ai.chat")
data class ChatProperties(
    val maxHistorySize: Int,
    val temperature: Double,
    val topK: Int,
    val topP: Double,
    val repeatPenalty: Double
)
