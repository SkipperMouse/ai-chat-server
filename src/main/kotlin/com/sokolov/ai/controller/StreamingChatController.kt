package com.sokolov.ai.controller

import com.sokolov.ai.service.ChatService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
class StreamingChatController(val chatService: ChatService) {

    @GetMapping("/chat-stream/{chatId}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun  getChatStream(@PathVariable chatId: Long, @RequestParam("userPrompt") prompt: String): SseEmitter {
        return chatService.proceedInteractionWithStreaming(chatId, prompt)
    }
}