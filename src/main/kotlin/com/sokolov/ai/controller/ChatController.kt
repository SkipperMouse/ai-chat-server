package com.sokolov.ai.controller

import com.sokolov.ai.service.ChatService
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller("/")
class ChatController(val chatService: ChatService) {

    @GetMapping
    fun getAllChats(model: ModelMap): String {
        //todo we extract full chat history instead of chat names. Fix it
        model.addAttribute("chats", chatService.getAllChats())
        return "chat"
    }

    @GetMapping("/chat/{id}")
    fun getChat(@PathVariable id: Long, model: ModelMap): String {
        val chat = chatService.getChat(id)
        model.addAttribute("chats", chatService.getAllChats())
        model.addAttribute("chat", chat)
        return "chat"
    }

    @PostMapping("/chat/new")
    fun addChat(@RequestParam("title") title: String): String {
        val chat = chatService.addChat(title)
        return "redirect:/chat/" + requireNotNull(chat.id)
    }

    @PostMapping("/chat/{chatId}/delete")
    fun deleteChat(@PathVariable chatId: Long): String {
        chatService.deleteChat(chatId)
        return "redirect:/"
    }

    @PostMapping("/chat/{chatId}/entry")
    fun talkToModel(@PathVariable chatId: Long, @RequestParam prompt: String): String {
        println("talkToModel")
        chatService.proceedInteraction(chatId, prompt)
        return "redirect:/chat/$chatId"

    }

}