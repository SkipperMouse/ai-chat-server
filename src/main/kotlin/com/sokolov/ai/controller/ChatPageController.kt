package com.sokolov.ai.controller

import com.sokolov.ai.service.ChatService
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/chats")
class ChatPageController(val chatService: ChatService) {

    @GetMapping
    fun getAllChats(model: ModelMap): String {
        //todo we extract full chat history instead of chat names. Fix it
        model.addAttribute("chats", chatService.getAllChats())
        return "chat"
    }

    @GetMapping("/{id}")
    fun getChat(@PathVariable id: Long, model: ModelMap): String {
        val chat = chatService.getChat(id)
        model.addAttribute("chats", chatService.getAllChats())
        model.addAttribute("chat", chat)
        return "chat"
    }

    @PostMapping
    fun addChat(@RequestParam("title") title: String): String {
        val chat = chatService.addChat(title)
        return "redirect:/chats/" + requireNotNull(chat.id)
    }

    @DeleteMapping("/{chatId}")
    fun deleteChat(@PathVariable chatId: Long): String {
        chatService.deleteChat(chatId)
        return "redirect:/chats"
    }

    @PostMapping("/{chatId}/messages")
    fun messageToModel(@PathVariable chatId: Long, @RequestParam prompt: String): String {
        chatService.proceedInteraction(chatId, prompt)
        return "redirect:/chats/$chatId"
    }
}