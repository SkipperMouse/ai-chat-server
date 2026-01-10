package com.sokolov.ai.config

import com.sokolov.ai.infrastructure.PostgresChatMemory
import com.sokolov.ai.repository.ChatRepository
import com.sokolov.ai.utils.PROMPT_TEMPLATE
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatConfiguration(val chatRepository: ChatRepository) {

    @Bean
    fun chatClient(
        builder: ChatClient.Builder,
        historyAdviser: MessageChatMemoryAdvisor,
        ragAdvisor: QuestionAnswerAdvisor
    ): ChatClient = builder.defaultAdvisors(
        historyAdviser,
        SimpleLoggerAdvisor.builder().build(),
        ragAdvisor
    )
        .build()

    @Bean
    fun getHistoryAdviser(chatMemory: PostgresChatMemory): MessageChatMemoryAdvisor =
        MessageChatMemoryAdvisor.builder(chatMemory).build()


    @Bean
    fun getRagAdviser(vectorStore: VectorStore): QuestionAnswerAdvisor =
        QuestionAnswerAdvisor.builder(vectorStore)
            .promptTemplate(PromptTemplate(PROMPT_TEMPLATE))
//            .searchRequest(SearchRequest.builder()
//                .query()
//                .topK(4))
            .build()


    @Bean
    fun postgresChatMemory(): PostgresChatMemory = PostgresChatMemory(
        chatRepository = chatRepository,
        maxMessages = 4
    )
}