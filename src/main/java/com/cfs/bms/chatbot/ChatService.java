package com.cfs.bms.chatbot;

import com.cfs.bms.dto.CartRequestDto;
import com.cfs.bms.dto.SeatRecommendationDto;
import com.cfs.bms.model.Movie;
import com.cfs.bms.model.Show;
import com.cfs.bms.repository.MovieRepository;
import com.cfs.bms.repository.ShowRepository;
import com.cfs.bms.service.CartService;
import com.cfs.bms.service.SeatRecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Core chatbot service with state-machine based conversation flow.
 * Steps: GREETING → MOVIE → CITY → DATE → TIME → SEATS_COUNT → SEATS → CONFIRM → DONE
 */
@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private static final String SYSTEM_PROMPT =
            "You are CineBot, a friendly AI assistant for BookMyShow. " +
            "Your job is to help users book movie tickets. " +
            "Be conversational, enthusiastic about movies, and keep responses concise (2-3 sentences max). " +
            "When guiding users through booking, always sound helpful and natural.";

    // In-memory session store (use Redis in production)
    private final Map<String, BookingContext> sessions = new ConcurrentHashMap<>();

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private SeatRecommendationService seatRecommendationService;

    @Autowired
    private CartService cartService;

    @Autowired
    private GrokApiClient grokApiClient;

    /**
     * Creates a new chat session and returns the greeting message.
     */
    public Map<String, Object> startSession(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        BookingContext ctx = new BookingContext();
        ctx.setSessionId(sessionId);
        ctx.setSelectedUserId(userId);
        ctx.setStep("GREETING");
        sessions.put(sessionId, ctx);

        List<Movie> movies = movieRepository.findAll();
        List<String> movieOptions = movies.stream()
                .map(Movie::getTitle)
                .limit(8)
                .collect(Collectors.toList());

        ChatMessage greeting = new ChatMessage();
        greeting.setRole("assistant");
        greeting.setContent("🎬 Hey there! I'm CineBot, your personal movie booking assistant! " +
                "I'll help you find and book the perfect movie experience. " +
                "Which movie are you looking to watch today?");
        greeting.setQuickOptions(movieOptions);
        greeting.setStep("MOVIE");

        ctx.setStep("MOVIE");

        return Map.of("sessionId", sessionId, "message", greeting);
    }

    /**
     * Processes a user message and returns the next chatbot response.
     */
    public ChatMessage processMessage(ChatRequest request) {
        BookingContext ctx = sessions.get(request.getSessionId());
        if (ctx == null) {
            ChatMessage err = new ChatMessage();
            err.setRole("assistant");
            err.setContent("I'm sorry, I couldn't find your session. Please start a new conversation!");
            err.setStep("ERROR");
            return err;
        }

        String userMsg = request.getMessage() != null ? request.getMessage().trim() : "";
        ctx.addMessage("user", userMsg);

        ChatMessage response = processStep(ctx, userMsg);
        ctx.addMessage("assistant", response.getContent());

        // Try to enhance with Grok AI
        if (grokApiClient.isAvailable() && !"CONFIRM".equals(ctx.getStep()) && !"DONE".equals(ctx.getStep())) {
            String aiResponse = grokApiClient.enhanceResponse(SYSTEM_PROMPT, userMsg, ctx.getMessageHistory());
            if (aiResponse != null && !aiResponse.isBlank()) {
                // Append AI tone to rule-based structured content
                response.setContent(response.getContent() + "\n\n💡 " + aiResponse.trim());
            }
        }

        return response;
    }

    private ChatMessage processStep(BookingContext ctx, String userMsg) {
        return switch (ctx.getStep()) {
            case "MOVIE" -> handleMovieStep(ctx, userMsg);
            case "CITY" -> handleCityStep(ctx, userMsg);
            case "DATE" -> handleDateStep(ctx, userMsg);
            case "TIME" -> handleTimeStep(ctx, userMsg);
            case "SEATS_COUNT" -> handleSeatCountStep(ctx, userMsg);
            case "SEATS" -> handleSeatsStep(ctx, userMsg);
            case "CONFIRM" -> handleConfirmStep(ctx, userMsg);
            default -> buildMessage("MOVIE", "Which movie would you like to watch?", getMovieOptions());
        };
    }

    private ChatMessage handleMovieStep(BookingContext ctx, String userMsg) {
        List<Movie> movies = movieRepository.findAll();

        // Try to match movie by title (case-insensitive partial match)
        Optional<Movie> matched = movies.stream()
                .filter(m -> m.getTitle().toLowerCase().contains(userMsg.toLowerCase()))
                .findFirst();

        if (matched.isEmpty()) {
            List<String> options = movies.stream().map(Movie::getTitle).limit(8).collect(Collectors.toList());
            return buildMessage("MOVIE",
                    "I couldn't find a movie matching \"" + userMsg + "\". Here are currently showing movies:",
                    options);
        }

        Movie movie = matched.get();
        ctx.setSelectedMovieId(movie.getId());
        ctx.setSelectedMovieTitle(movie.getTitle());
        ctx.setStep("CITY");

        // Get available cities
        List<String> cities = showRepository.findByMovieId(movie.getId()).stream()
                .map(s -> s.getScreen().getTheater().getCity())
                .distinct()
                .collect(Collectors.toList());

        if (cities.isEmpty()) {
            return buildMessage("MOVIE",
                    "Great choice! Unfortunately \"" + movie.getTitle() + "\" has no shows available right now. " +
                    "Please choose another movie:", getMovieOptions());
        }

        return buildMessage("CITY",
                "🎬 Excellent choice! **" + movie.getTitle() + "** it is! " +
                "Which city are you in?", cities);
    }

    private ChatMessage handleCityStep(BookingContext ctx, String userMsg) {
        List<Show> shows = showRepository.findByMovie_IdAndScreen_Theater_City(ctx.getSelectedMovieId(), userMsg);

        if (shows.isEmpty()) {
            // Try case-insensitive search
            List<Show> allShows = showRepository.findByMovieId(ctx.getSelectedMovieId());
            shows = allShows.stream()
                    .filter(s -> s.getScreen().getTheater().getCity().toLowerCase().contains(userMsg.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (shows.isEmpty()) {
            List<String> availableCities = showRepository.findByMovieId(ctx.getSelectedMovieId()).stream()
                    .map(s -> s.getScreen().getTheater().getCity())
                    .distinct()
                    .collect(Collectors.toList());
            return buildMessage("CITY",
                    "No shows for " + ctx.getSelectedMovieTitle() + " in \"" + userMsg + "\". Available cities:",
                    availableCities);
        }

        ctx.setSelectedCity(userMsg);
        ctx.setStep("DATE");

        // Get available dates
        List<String> dates = shows.stream()
                .map(s -> s.getStartTime().toLocalDate().toString())
                .distinct()
                .sorted()
                .limit(7)
                .collect(Collectors.toList());

        return buildMessage("DATE",
                "📍 " + userMsg + " looks great! What date would you like to watch the movie?",
                dates);
    }

    private ChatMessage handleDateStep(BookingContext ctx, String userMsg) {
        LocalDate selectedDate;
        try {
            selectedDate = LocalDate.parse(userMsg.trim());
        } catch (Exception e) {
            // Try to parse common formats
            try {
                selectedDate = LocalDate.parse(userMsg.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception e2) {
                return buildMessage("DATE",
                        "Please select a date from the options, or type it as YYYY-MM-DD:", getDateOptions(ctx));
            }
        }

        ctx.setSelectedDate(selectedDate.toString());
        ctx.setStep("TIME");

        // Get shows on this date
        LocalDateTime startOfDay = selectedDate.atStartOfDay();
        LocalDateTime endOfDay = selectedDate.atTime(23, 59, 59);

        List<Show> shows = showRepository.findByMovie_IdAndScreen_Theater_City(
                ctx.getSelectedMovieId(), ctx.getSelectedCity()
        ).stream()
                .filter(s -> !s.getStartTime().isBefore(startOfDay) && !s.getStartTime().isAfter(endOfDay))
                .collect(Collectors.toList());

        if (shows.isEmpty()) {
            return buildMessage("DATE",
                    "No shows on " + userMsg + " for " + ctx.getSelectedMovieTitle() + " in " + ctx.getSelectedCity() + ". Please pick another date:",
                    getDateOptions(ctx));
        }

        List<String> timeOptions = shows.stream()
                .map(s -> s.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                          " - " + s.getScreen().getTheater().getName() +
                          " (ID:" + s.getId() + ")")
                .collect(Collectors.toList());

        return buildMessage("TIME",
                "📅 " + selectedDate + " is set! Choose a show time:", timeOptions);
    }

    private ChatMessage handleTimeStep(BookingContext ctx, String userMsg) {
        // Extract show ID from format "HH:mm - Theater Name (ID:123)"
        Long showId = null;
        if (userMsg.contains("ID:")) {
            try {
                String idStr = userMsg.substring(userMsg.lastIndexOf("ID:") + 3).replace(")", "").trim();
                showId = Long.parseLong(idStr);
            } catch (Exception e) {
                // ignore
            }
        }

        if (showId == null) {
            return buildMessage("TIME",
                    "Please select a show time from the options provided. " +
                    "What time works for you?", getTimeOptions(ctx));
        }

        ctx.setSelectedShowId(showId);
        ctx.setSelectedTime(userMsg);
        ctx.setStep("SEATS_COUNT");

        return buildMessage("SEATS_COUNT",
                "🪑 How many seats do you need? (1-10)",
                List.of("1", "2", "3", "4", "5", "6"));
    }

    private ChatMessage handleSeatCountStep(BookingContext ctx, String userMsg) {
        int count;
        try {
            count = Integer.parseInt(userMsg.trim());
            if (count < 1 || count > 10) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            return buildMessage("SEATS_COUNT",
                    "Please enter a number between 1 and 10 for the number of seats:",
                    List.of("1", "2", "3", "4", "5", "6"));
        }

        ctx.setNumberOfSeats(count);
        ctx.setStep("SEATS");

        // Get AI seat recommendations
        try {
            SeatRecommendationDto recommendation = seatRecommendationService.recommendSeats(
                    ctx.getSelectedShowId(), count);

            if (recommendation.getRecommendedSeats().isEmpty()) {
                return buildMessage("MOVIE",
                        "Sorry, there are no available seats for this show. Please choose another show time.",
                        getMovieOptions());
            }

            List<Long> recommendedIds = recommendation.getRecommendedSeats().stream()
                    .map(s -> s.getId())
                    .collect(Collectors.toList());
            ctx.setRecommendedSeatIds(recommendedIds);

            String seatList = recommendation.getRecommendedSeats().stream()
                    .map(s -> s.getSeat().getSeatNumber() + " (" + s.getSeat().getSeatType() + " - ₹" + s.getPrice() + ")")
                    .collect(Collectors.joining(", "));

            double total = recommendation.getRecommendedSeats().stream()
                    .mapToDouble(s -> s.getPrice()).sum();

            String msg = "🤖 **AI Seat Recommendation:**\n" +
                    "Recommended seats: **" + seatList + "**\n" +
                    recommendation.getExplanation() + "\n\n" +
                    "💰 Total: ₹" + String.format("%.2f", total) + "\n\n" +
                    "Would you like to proceed with these seats, or go to the seat layout to choose manually?";

            return buildMessage("CONFIRM", msg, List.of("✅ Proceed with recommended seats", "🪑 Choose seats manually"));

        } catch (Exception e) {
            logger.error("Seat recommendation failed: {}", e.getMessage());
            return buildMessage("CONFIRM",
                    "Please proceed to seat selection. " +
                    "How many seats did you want? You can choose your seats on the next page.",
                    List.of("Proceed to booking"));
        }
    }

    private ChatMessage handleSeatsStep(BookingContext ctx, String userMsg) {
        return handleConfirmStep(ctx, userMsg);
    }

    private ChatMessage handleConfirmStep(BookingContext ctx, String userMsg) {
        boolean isConfirmed = userMsg.toLowerCase().contains("proceed") ||
                userMsg.toLowerCase().contains("yes") ||
                userMsg.toLowerCase().contains("confirm") ||
                userMsg.toLowerCase().contains("✅");

        if (!isConfirmed) {
            ctx.setStep("DONE");
            ChatMessage msg = new ChatMessage();
            msg.setRole("assistant");
            msg.setContent("No problem! You can browse the seat layout and pick your preferred seats. " +
                    "Check out the booking page to continue.");
            msg.setStep("DONE");
            msg.setQuickOptions(List.of("Start new booking"));
            return msg;
        }

        // Create cart
        try {
            if (ctx.getSelectedUserId() == null) {
                ctx.setStep("DONE");
                return buildMessage("DONE",
                        "Please log in to complete your booking. Once logged in, come back and I'll help you pick up where we left off!",
                        List.of("Login", "Start over"));
            }

            CartRequestDto cartRequest = new CartRequestDto();
            cartRequest.setUserId(ctx.getSelectedUserId());
            cartRequest.setShowId(ctx.getSelectedShowId());
            cartRequest.setSeatIds(ctx.getRecommendedSeatIds());

            var cart = cartService.createCart(cartRequest);
            ctx.setCartId(cart.getCartId());
            ctx.setStep("DONE");

            double total = cart.getTotalPrice();
            String movieTitle = cart.getShow().getMovie().getTitle();
            String theater = cart.getShow().getScreen().getTheater().getName();

            ChatMessage confirmMsg = new ChatMessage();
            confirmMsg.setRole("assistant");
            confirmMsg.setContent(
                    "🎉 **Booking Summary:**\n\n" +
                    "🎬 Movie: **" + movieTitle + "**\n" +
                    "🏛️ Theater: **" + theater + "**\n" +
                    "📅 Date & Time: **" + ctx.getSelectedDate() + " " + ctx.getSelectedTime() + "**\n" +
                    "🪑 Seats: " + cart.getSeats().size() + " seats reserved\n" +
                    "💰 Total: **₹" + String.format("%.2f", total) + "**\n\n" +
                    "Your cart is ready! Complete payment to confirm your booking. " +
                    "Cart expires in 15 minutes."
            );
            confirmMsg.setCartId(cart.getCartId());
            confirmMsg.setStep("DONE");
            confirmMsg.setQuickOptions(List.of("View Cart & Pay", "Start new booking"));
            return confirmMsg;

        } catch (Exception e) {
            logger.error("Cart creation failed in chatbot: {}", e.getMessage());
            ctx.setStep("MOVIE");
            return buildMessage("MOVIE",
                    "Oops! Something went wrong while creating your cart. Let's start fresh — which movie would you like?",
                    getMovieOptions());
        }
    }

    // Helpers
    private ChatMessage buildMessage(String step, String content, List<String> options) {
        ChatMessage msg = new ChatMessage();
        msg.setRole("assistant");
        msg.setContent(content);
        msg.setQuickOptions(options);
        msg.setStep(step);
        return msg;
    }

    private List<String> getMovieOptions() {
        return movieRepository.findAll().stream()
                .map(Movie::getTitle).limit(8).collect(Collectors.toList());
    }

    private List<String> getDateOptions(BookingContext ctx) {
        if (ctx.getSelectedMovieId() == null || ctx.getSelectedCity() == null) return List.of();
        return showRepository.findByMovie_IdAndScreen_Theater_City(ctx.getSelectedMovieId(), ctx.getSelectedCity())
                .stream()
                .map(s -> s.getStartTime().toLocalDate().toString())
                .distinct().sorted().limit(7).collect(Collectors.toList());
    }

    private List<String> getTimeOptions(BookingContext ctx) {
        if (ctx.getSelectedMovieId() == null || ctx.getSelectedCity() == null || ctx.getSelectedDate() == null)
            return List.of();
        LocalDate date = LocalDate.parse(ctx.getSelectedDate());
        return showRepository.findByMovie_IdAndScreen_Theater_City(ctx.getSelectedMovieId(), ctx.getSelectedCity())
                .stream()
                .filter(s -> s.getStartTime().toLocalDate().equals(date))
                .map(s -> s.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                          " - " + s.getScreen().getTheater().getName() + " (ID:" + s.getId() + ")")
                .collect(Collectors.toList());
    }

    public BookingContext getSession(String sessionId) {
        return sessions.get(sessionId);
    }
}
