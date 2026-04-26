package com.cfs.bms.chatbot;

import lombok.Data;

import java.util.List;

/**
 * Holds the in-memory state of a chatbot booking conversation.
 * One instance per session, stored in ChatService session map.
 */
@Data
public class BookingContext {
    private String sessionId;
    private String step = "GREETING"; // GREETING, MOVIE, CITY, DATE, SEATS_COUNT, SEATS, CONFIRM, DONE

    // Collected data
    private String selectedMovieTitle;
    private Long selectedMovieId;
    private String selectedCity;
    private String selectedDate;        // "2024-12-25"
    private String selectedTime;        // "14:30"
    private Long selectedShowId;
    private Integer numberOfSeats = 1;
    private Long selectedUserId;

    // After recommendation
    private List<Long> recommendedSeatIds;
    private String cartId;

    // Conversation history for Grok context
    private List<String> messageHistory = new java.util.ArrayList<>();

    public void addMessage(String role, String content) {
        messageHistory.add(role + ": " + content);
    }
}
