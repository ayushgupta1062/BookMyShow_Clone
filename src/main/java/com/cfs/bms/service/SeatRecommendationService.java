package com.cfs.bms.service;

import com.cfs.bms.dto.SeatDto;
import com.cfs.bms.dto.SeatRecommendationDto;
import com.cfs.bms.dto.ShowSeatDto;
import com.cfs.bms.model.ShowSeat;
import com.cfs.bms.repository.ShowSeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Seat Recommendation Engine.
 * Scores available seats based on position (row + column).
 * Middle rows and center columns receive highest scores.
 * First rows and corner seats receive penalties.
 */
@Service
public class SeatRecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(SeatRecommendationService.class);

    @Autowired
    private ShowSeatRepository showSeatRepository;

    /**
     * Recommends the best 'count' available seats for the given show.
     * Scoring algorithm:
     * - Row score: distance from middle row (lower = better, inverted to higher score)
     * - Col score: distance from center column (lower = better, inverted)
     * - Penalty: first row gets -20, last row gets -10, corner seats get -15
     */
    public SeatRecommendationDto recommendSeats(Long showId, int count) {
        List<ShowSeat> availableSeats = showSeatRepository.findByShowIdAndStatus(showId, "AVAILABLE");

        if (availableSeats.isEmpty()) {
            return new SeatRecommendationDto(Collections.emptyList(),
                    "No seats available for this show.", 0);
        }

        // Parse seat positions from seatNumber (e.g., "A5", "B10", "C3")
        // Row = letter (A=0, B=1, ...), Col = number
        List<SeatScore> scoredSeats = new ArrayList<>();

        // Determine grid dimensions
        int maxRow = 0;
        int maxCol = 0;
        for (ShowSeat ss : availableSeats) {
            String seatNum = ss.getSeat().getSeatNumber();
            int row = extractRow(seatNum);
            int col = extractCol(seatNum);
            maxRow = Math.max(maxRow, row);
            maxCol = Math.max(maxCol, col);
        }

        double middleRow = maxRow / 2.0;
        double middleCol = maxCol / 2.0;

        for (ShowSeat ss : availableSeats) {
            String seatNum = ss.getSeat().getSeatNumber();
            int row = extractRow(seatNum);
            int col = extractCol(seatNum);

            // Base score: closer to center = higher score (max 100)
            double rowDistanceScore = (1.0 - Math.abs(row - middleRow) / (maxRow + 1)) * 50;
            double colDistanceScore = (1.0 - Math.abs(col - middleCol) / (maxCol + 1)) * 50;
            double score = rowDistanceScore + colDistanceScore;

            // Penalties
            if (row == 0) score -= 20; // Front row penalty (neck strain)
            if (row == maxRow) score -= 10; // Last row penalty (far from screen)
            if (col == 0 || col == maxCol) score -= 15; // Corner penalty (angled view)

            // Bonus for premium seat types
            String seatType = ss.getSeat().getSeatType();
            if ("GOLD".equalsIgnoreCase(seatType)) score += 5;

            scoredSeats.add(new SeatScore(ss, score));
        }

        // Sort by score descending
        scoredSeats.sort((a, b) -> Double.compare(b.score, a.score));

        // Take top 'count' seats
        int take = Math.min(count, scoredSeats.size());
        List<ShowSeat> recommended = scoredSeats.stream()
                .limit(take)
                .map(s -> s.showSeat)
                .collect(Collectors.toList());

        // Build explanation
        String explanation = buildExplanation(recommended, maxRow, maxCol);

        // Map to DTOs
        List<ShowSeatDto> seatDtos = recommended.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        logger.info("Recommended {} seats for showId={}", take, showId);
        return new SeatRecommendationDto(seatDtos, explanation, availableSeats.size());
    }

    private int extractRow(String seatNumber) {
        if (seatNumber == null || seatNumber.isEmpty()) return 0;
        char firstChar = seatNumber.charAt(0);
        if (Character.isLetter(firstChar)) {
            return Character.toUpperCase(firstChar) - 'A';
        }
        return 0;
    }

    private int extractCol(String seatNumber) {
        if (seatNumber == null || seatNumber.isEmpty()) return 0;
        try {
            String numPart = seatNumber.replaceAll("[^0-9]", "");
            return numPart.isEmpty() ? 0 : Integer.parseInt(numPart) - 1;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String buildExplanation(List<ShowSeat> seats, int maxRow, int maxCol) {
        if (seats.isEmpty()) return "No seats available.";

        List<String> seatNums = seats.stream()
                .map(s -> s.getSeat().getSeatNumber())
                .collect(Collectors.toList());

        String seatList = String.join(", ", seatNums);

        return String.format(
                "Seats %s are recommended because they offer an optimal central viewing position. " +
                "These seats are located in the middle section of the %d-row theater, " +
                "providing the best angle and distance from the screen. " +
                "Note: Final seat selection is up to you!",
                seatList, maxRow + 1
        );
    }

    private ShowSeatDto mapToDto(ShowSeat seat) {
        ShowSeatDto dto = new ShowSeatDto();
        dto.setId(seat.getId());
        dto.setStatus(seat.getStatus());
        dto.setPrice(seat.getPrice());

        SeatDto baseSeat = new SeatDto();
        baseSeat.setId(seat.getSeat().getId());
        baseSeat.setSeatNumber(seat.getSeat().getSeatNumber());
        baseSeat.setSeatType(seat.getSeat().getSeatType());
        baseSeat.setBasePrice(seat.getSeat().getBasePrice());
        dto.setSeat(baseSeat);

        return dto;
    }

    private static class SeatScore {
        final ShowSeat showSeat;
        final double score;

        SeatScore(ShowSeat showSeat, double score) {
            this.showSeat = showSeat;
            this.score = score;
        }
    }
}
