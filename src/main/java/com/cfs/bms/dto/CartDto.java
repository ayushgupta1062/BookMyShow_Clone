package com.cfs.bms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long id;
    private String cartId;
    private UserDto user;
    private ShowDto show;
    private List<ShowSeatDto> seats;
    private Double totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String cartLink;
}
