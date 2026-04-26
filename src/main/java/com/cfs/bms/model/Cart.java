package com.cfs.bms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cartId; // UUID public identifier

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ElementCollection
    @CollectionTable(name = "cart_seat_ids", joinColumns = @JoinColumn(name = "cart_id"))
    @Column(name = "show_seat_id")
    private List<Long> seatIds;

    @Column(nullable = false)
    private Double totalPrice;

    @Column(nullable = false)
    private String status; // ACTIVE, EXPIRED, CONVERTED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime expiresAt;
}
