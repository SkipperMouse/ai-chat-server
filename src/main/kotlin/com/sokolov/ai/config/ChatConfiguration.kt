package com.sokolov.ai.config

import com.sokolov.ai.infrastructure.PostgresChatMemory
import com.sokolov.ai.infrastructure.advisors.ExpansionQueryAdvisor
import com.sokolov.ai.infrastructure.advisors.RagAdvisor
import com.sokolov.ai.repository.ChatRepository
import com.sokolov.ai.utils.PROMPT_TEMPLATE
import com.sokolov.ai.utils.SYSTEM_PROMPT
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.client.advisor.api.Advisor
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.ollama.api.OllamaChatOptions
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatConfiguration(
    private val chatProperties: ChatProperties,
    private val ragAdvisorProperties: RagAdviserProperties,
    private val chatRepository: ChatRepository,
    private val chatModel: ChatModel
) {

    @Bean
    fun chatClient(
        builder: ChatClient.Builder,
        advisors: List<Advisor>
    ): ChatClient = builder
        .defaultAdvisors(advisors)
        .defaultOptions(
            OllamaChatOptions.builder()
                .temperature(chatProperties.temperature)
                .topK(chatProperties.topK)
                .topP(chatProperties.topP)
                .repeatPenalty(chatProperties.repeatPenalty)
                .build()
        )
        .defaultSystem { SYSTEM_PROMPT }
        .build()

    @Bean
    fun getOrderedAdvisors(
        historyAdviserBuilder: MessageChatMemoryAdvisor.Builder,
        ragAdvisorBuilder: RagAdvisor.Builder
    ): List<Advisor> {
        var count = 0
        return listOf(
            ExpansionQueryAdvisor.builder(chatModel).order(++count).build(),
            SimpleLoggerAdvisor.builder().order(++count).build(),
            historyAdviserBuilder.order(++count).build(),
            SimpleLoggerAdvisor.builder().order(++count).build(),
            ragAdvisorBuilder.order(++count).build(),
            SimpleLoggerAdvisor.builder().order(++count).build()
        )
    }

    @Bean
    fun getHistoryAdviserBuilder(chatMemory: PostgresChatMemory): MessageChatMemoryAdvisor.Builder =
        MessageChatMemoryAdvisor.builder(chatMemory)


    @Bean
    fun getRagAdviserBuilder(vectorStore: VectorStore): RagAdvisor.Builder =
        RagAdvisor.builder(vectorStore)
            .searchRequest(
                SearchRequest.builder()
                    .topK(ragAdvisorProperties.topK)
                    .similarityThreshold(ragAdvisorProperties.similarityThreshold)
                    .build()
            )
            .promptTemplate(PromptTemplate(PROMPT_TEMPLATE))

    @Bean
    fun postgresChatMemory(): PostgresChatMemory = PostgresChatMemory(
        chatRepository = chatRepository,
        maxMessages = chatProperties.maxHistorySize
    )
}