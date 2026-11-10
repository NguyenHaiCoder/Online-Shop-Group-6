package com.swd392.onlineshop.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Body cua chatbox: cau hoi moi + cac luot truoc do (role = user | model). */
@Getter
@Setter
@NoArgsConstructor
public class ChatForm {

    private String message;
    private List<Turn> history = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Turn {
        private String role;
        private String text;
    }
}
