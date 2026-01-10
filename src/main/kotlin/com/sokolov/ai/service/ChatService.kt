package com.sokolov.ai.service

import com.sokolov.ai.domain.chat.Chat
import com.sokolov.ai.domain.chat.ChatEntry
import com.sokolov.ai.exception.NotFoundException
import com.sokolov.ai.repository.ChatRepository
import jakarta.transaction.Transactional
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.MessageType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Service
class ChatService(val chatRepository: ChatRepository, val chatClient: ChatClient) {
    @Autowired
    @Lazy
    private lateinit var self: ChatService


    fun getAllChats(): List<Chat> {
        println("getAllChats")
        return chatRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
    }

    fun getChat(id: Long): Chat {
        println("getChat=${id}")
        return chatRepository.findById(id).orElseThrow { NotFoundException(id) }
    }

    fun addChat(title: String): Chat {
        println("add chat=${title}")
        return chatRepository.save(Chat(title = title))
    }

    fun deleteChat(chatId: Long) {
        println("delete by id $chatId")
        chatRepository.deleteById(chatId)
    }

    fun proceedInteraction(chatId: Long, prompt: String) {
        self.addChatEntry(chatId, prompt, MessageType.USER)
        val answer = chatClient.prompt().user(prompt).call().content()
        self.addChatEntry(chatId, requireNotNull(answer), MessageType.ASSISTANT)
    }


    fun proceedInteractionWithStreaming(chatId: Long, prompt: String): SseEmitter {
        val emitter = SseEmitter(0)

        chatClient.prompt()
            .advisors { advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId) }
            .user(prompt).stream()
            .chatResponse()
            .subscribe(
                { processToken(it.result.output, emitter) },
                emitter::completeWithError,
                { onComplete(emitter) }
            )
        return emitter
    }

    @Transactional
    fun addChatEntry(chatId: Long, content: String, role: MessageType) {
        val chat = getChat(chatId)
        chat.addEntry(ChatEntry(content = content, role = role))
    }

    private fun processToken(result: AssistantMessage, emitter: SseEmitter) {
        emitter.send(result)
    }

    private fun onComplete(emitter: SseEmitter) {
        emitter.complete()
    }
}
