package com.sokolov.ai.service

import com.sokolov.ai.domain.chat.Chat
import com.sokolov.ai.domain.chat.ChatMessage
import com.sokolov.ai.exception.NotFoundException
import com.sokolov.ai.repository.ChatRepository
import com.sokolov.ai.repository.MessageRepository
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.MessageType
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val chatClient: ChatClient,
) {
    private val log = LoggerFactory.getLogger(ChatService::class.java)


    fun getAllChats(): List<Chat> {
        return chatRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
    }

    fun getChat(id: Long): Chat {
        return chatRepository.findById(id).orElseThrow { NotFoundException(id) }
    }

    fun addChat(title: String): Chat {
        return chatRepository.save(Chat(title = title))
    }

    fun deleteChat(chatId: Long) {
        chatRepository.deleteById(chatId)
    }

    fun proceedInteraction(chatId: Long, prompt: String) {
        addMessage(chatId, prompt, MessageType.USER)
        val answer = chatClient.prompt().user(prompt).call().content()
        addMessage(chatId, requireNotNull(answer), MessageType.ASSISTANT)
    }


    fun proceedInteractionWithStreaming(chatId: Long, prompt: String): SseEmitter {
        log.debug("proceedInteractionWithStreaming. chatId=${chatId}, prompt=${prompt}")
        val emitter = SseEmitter(0)
        chatClient.prompt()
            .advisors { advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId) }
            .user(prompt)
            .stream()
            .chatResponse()
            .subscribe(
                { processToken(it.result.output, emitter) },
                { ex: Throwable -> completeWithError(emitter, ex) },
                { onComplete(emitter) }
            )
        return emitter
    }

    fun addMessage(chatId: Long, content: String, role: MessageType) {
        val chat = getChat(chatId)
        messageRepository.save(
            ChatMessage(
                content = content,
                role = role,
                chatId = requireNotNull(chat.id)
            )
        )
    }

    private fun processToken(result: AssistantMessage, emitter: SseEmitter) {
        emitter.send(result)
    }

    private fun completeWithError(emitter: SseEmitter, ex: Throwable) {
        log.error("streaming error for chat", ex)
        emitter.completeWithError(ex)
    }

    private fun onComplete(emitter: SseEmitter) {
        emitter.complete()
        log.debug("answer is completed, emitter is closed")
    }
}
