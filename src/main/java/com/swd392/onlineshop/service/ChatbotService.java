package com.swd392.onlineshop.service;

import com.swd392.onlineshop.dto.request.ChatForm;
import com.swd392.onlineshop.dto.response.ChatReply;

/** Tro ly ao o goc man hinh, goi Google Gemini (free tier). */
public interface ChatbotService {

    ChatReply ask(ChatForm form);
}
