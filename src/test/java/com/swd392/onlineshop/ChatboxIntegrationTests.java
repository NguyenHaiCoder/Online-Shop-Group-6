package com.swd392.onlineshop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class ChatboxIntegrationTests {

    @Autowired
    WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void anonymousCanCallChatWithoutCsrfAndGetsFriendlyMessageWhenKeyMissing() throws Exception {
        mvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Shop có giao hàng không?\",\"history\":[{\"role\":\"model\",\"text\":\"Xin chào\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.reply").value(containsString("GEMINI_API_KEY")));
    }

    @Test
    void emptyMessageIsRejectedPolitely() throws Exception {
        mvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"   \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    void publicPagesLoadUiJsWhichMountsTheChatbox() throws Exception {
        for (String url : new String[]{"/", "/products", "/blogs", "/cart", "/feedback", "/auth/login"}) {
            mvc.perform(get(url).accept(MediaType.TEXT_HTML))
                    .andExpect(status().isOk())
                    .andExpect(content().string(not(containsString("Lỗi máy chủ"))))
                    .andExpect(content().string(containsString("/js/ui.js")));
        }
        mvc.perform(get("/js/ui.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("chatbox.js")));
        mvc.perform(get("/js/chatbox.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("api/chat")));
    }
}
