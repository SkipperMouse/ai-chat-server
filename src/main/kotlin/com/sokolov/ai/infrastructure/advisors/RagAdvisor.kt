package com.sokolov.ai.infrastructure.advisors

import com.sokolov.ai.infrastructure.ranker.OkapiBm25RerankEngine
import com.sokolov.ai.utils.CONTEXT
import com.sokolov.ai.utils.EMPTY_CONTEXT
import com.sokolov.ai.utils.ENRICHED_PROMPT
import com.sokolov.ai.utils.QUESTION
import org.springframework.ai.chat.client.ChatClientRequest
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.client.advisor.api.AdvisorChain
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore

class RagAdvisor private constructor(
    private val vectorStore: VectorStore,
    private val promptTemplate: PromptTemplate,
    private val searchRequest: SearchRequest,
    private val order: Int
) : BaseAdvisor {

    override fun before(
        chatClientRequest: ChatClientRequest,
        advisorChain: AdvisorChain
    ): ChatClientRequest {
        val originalQuestion = chatClientRequest.prompt.userMessage.text
        val queryToRag = (chatClientRequest.context[ENRICHED_PROMPT] as? String?) ?: originalQuestion

        var retrievedDocs = vectorStore.similaritySearch(
            SearchRequest.from(searchRequest)
                .query(queryToRag)
                .topK(searchRequest.topK * 2) // increase number of documents from RAG for better ranking
                .build()
        )
        if (retrievedDocs.isEmpty()) return updateRequest(chatClientRequest, queryToRag, EMPTY_CONTEXT)
        val reranker = OkapiBm25RerankEngine()
        val rerankedDocs = reranker.rerank(retrievedDocs, queryToRag, searchRequest.topK)

        val llmContext = rerankedDocs
            .mapNotNull { it.text }
            .joinToString(System.lineSeparator())

        return updateRequest(chatClientRequest, queryToRag, llmContext)
    }

    override fun after(
        chatClientResponse: ChatClientResponse?,
        advisorChain: AdvisorChain?
    ): ChatClientResponse? {
        return chatClientResponse
    }

    override fun getOrder(): Int {
        return order
    }

    private fun updateRequest(
        chatClientRequest: ChatClientRequest,
        question: String,
        context: String
    ): ChatClientRequest {
        val finalPrompt = promptTemplate.render(
            mapOf(QUESTION to question, CONTEXT to context)
        )
        return chatClientRequest.mutate()
            .prompt(chatClientRequest.prompt.augmentUserMessage { UserMessage(finalPrompt) })
            .build()
    }

    companion object {
        fun builder(vectorStore: VectorStore): Builder = Builder(vectorStore)
        private val EMPTY_PROMPT_TEMPLATE = PromptTemplate.builder().build()

        private fun getSearchRequest(): SearchRequest = SearchRequest.builder()
            .topK(4)
            .similarityThreshold(0.65)
            .build()
    }

    class Builder(private val vectorStore: VectorStore) {
        private var order: Int = 0
        private var promptTemplate: PromptTemplate = EMPTY_PROMPT_TEMPLATE
        private var searchRequest: SearchRequest = getSearchRequest()

        fun order(order: Int) = apply { this.order = order }

        fun promptTemplate(promptTemplate: PromptTemplate) = apply { this.promptTemplate = promptTemplate }

        fun build(): RagAdvisor =
            RagAdvisor(vectorStore = vectorStore, promptTemplate, searchRequest, order = order)

        fun searchRequest(searchRequest: SearchRequest) = apply { this.searchRequest = searchRequest }
    }

}