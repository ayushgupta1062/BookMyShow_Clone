package com.cfs.bms.chatbot;

import lombok.Data;

@Data
public class ChatRequest {
    private String sessionId;
    private String message;
    private Long userId; // optional, for authenticated users
}
