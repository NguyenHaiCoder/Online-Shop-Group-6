package com.swd392.onlineshop.controller.publicsite;

import com.swd392.onlineshop.dto.request.ChatForm;
import com.swd392.onlineshop.dto.response.ChatReply;
import com.swd392.onlineshop.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Chatbox nho o goc man hinh, dung chung cho moi trang (fragments/chatbox). */
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatbotService chatbotService;

    @PostMapping("/api/chat")
    public ChatReply chat(@RequestBody ChatForm form) {
        return chatbotService.ask(form);
    }
}
