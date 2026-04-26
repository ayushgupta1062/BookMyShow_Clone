package com.cfs.bms.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String role;       // "user" | "assistant"
    private String content;
    private List<String> quickOptions; // Quick-reply buttons for UI
    private String cartId;     // Set when cart is created
    private String step;       // Current step for UI awareness
}
