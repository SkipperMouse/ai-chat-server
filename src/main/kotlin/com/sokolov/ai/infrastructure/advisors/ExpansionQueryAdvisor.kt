package com.sokolov.ai.infrastructure.advisors

import com.sokolov.ai.utils.ENRICHED_PROMPT
import com.sokolov.ai.utils.EXPANSION_QUERY_PROMPT
import com.sokolov.ai.utils.ORIGINAL_PROMPT
import com.sokolov.ai.utils.QUESTION
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.ChatClientRequest
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.client.advisor.api.AdvisorChain
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.ollama.api.OllamaChatOptions

private const val EXPANSION_RATIO = "EXPANSION_RATIO"

class ExpansionQueryAdvisor private constructor(
    private val chatClient: ChatClient,
    private val order: Int
) : BaseAdvisor {

    override fun before(
        chatClientRequest: ChatClientRequest,
        advisorChain: AdvisorChain
    ): ChatClientRequest {
        val originalPrompt = chatClientRequest.prompt.userMessage.text
        val enrichedPrompt = chatClient.prompt()
            .user(template.render(mapOf(Pair(QUESTION, originalPrompt))))
            .call()
            .content() ?: ""
        val ratio = enrichedPrompt
            .takeIf { it.isNotEmpty() }
            ?.let { originalPrompt.length.toDouble() / it.length } ?: 1
        return chatClientRequest.mutate()
            .context(ORIGINAL_PROMPT, originalPrompt)
            .context(ENRICHED_PROMPT, enrichedPrompt)
            .context(EXPANSION_RATIO, ratio)
            .build()


    }

    override fun after(
        chatClientResponse: ChatClientResponse,
        advisorChain: AdvisorChain
    ): ChatClientResponse {
        return chatClientResponse
    }

    override fun getOrder(): Int {
        return this.order
    }

    companion object {
        private val template = PromptTemplate.builder()
            .template(EXPANSION_QUERY_PROMPT)
            .build()

        fun builder(chatModel: ChatModel): Builder = Builder(
            ChatClient.builder(chatModel)
                .defaultOptions(
                    OllamaChatOptions.builder()
                        .temperature(0.0)
                        .topK(1)
                        .topP(0.1)
                        .repeatPenalty(1.0)
                        .build()
                )
                .build()
        )
    }

    class Builder(private var chatClient: ChatClient) {
        private var order: Int = 0

        fun order(order: Int) = apply { this.order = order }


        fun build(): ExpansionQueryAdvisor = ExpansionQueryAdvisor(chatClient, order)
    }
}