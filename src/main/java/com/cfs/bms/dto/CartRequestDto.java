package com.cfs.bms.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CartRequestDto {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Show ID is required")
    private Long showId;

    @NotEmpty(message = "At least one seat must be selected")
    private List<Long> seatIds;
}
