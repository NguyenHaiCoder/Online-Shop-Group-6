package com.swd392.onlineshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Cau tra loi cua chatbox. ok = false khi khong goi duoc model. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatReply {

    private String reply;
    private boolean ok;
}
