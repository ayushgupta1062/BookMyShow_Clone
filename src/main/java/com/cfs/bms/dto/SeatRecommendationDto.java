package com.cfs.bms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatRecommendationDto {
    private List<ShowSeatDto> recommendedSeats;
    private String explanation;
    private int totalAvailable;
}
