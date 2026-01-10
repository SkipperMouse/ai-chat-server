package com.sokolov.ai.config

import com.sokolov.ai.infrastructure.PostgresChatMemory
import com.sokolov.ai.repository.ChatRepository
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.api.Advisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatConfiguration(val chatRepository: ChatRepository) {

    @Bean
    fun chatClient(builder: ChatClient.Builder, advisor: Advisor): ChatClient {
        return builder.defaultAdvisors(advisor)
            .build()
    }

    @Bean
    fun advisor(chatMemory: ChatMemory): Advisor {
        return MessageChatMemoryAdvisor.builder(chatMemory).build()
    }

    @Bean
    fun ChatMemory(): ChatMemory {
        return PostgresChatMemory(
            chatRepository = chatRepository,
            maxMessages = 2
        )
    }
}