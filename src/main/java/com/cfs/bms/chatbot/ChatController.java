package com.cfs.bms.chatbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * Start a new chat session.
     * Returns sessionId and greeting message.
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startSession(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(chatService.startSession(userId));
    }

    /**
     * Send a message and get chatbot response.
     */
    @PostMapping("/message")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatRequest request) {
        ChatMessage response = chatService.processMessage(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current session state (for debugging / session recovery).
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getSession(@PathVariable String sessionId) {
        BookingContext ctx = chatService.getSession(sessionId);
        if (ctx == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ctx);
    }
}
