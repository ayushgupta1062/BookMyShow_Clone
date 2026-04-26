package com.cfs.bms.controller;

import com.cfs.bms.dto.BookingDto;
import com.cfs.bms.dto.CartDto;
import com.cfs.bms.dto.CartRequestDto;
import com.cfs.bms.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<CartDto> createCart(@Valid @RequestBody CartRequestDto request) {
        return new ResponseEntity<>(cartService.createCart(request), HttpStatus.CREATED);
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartDto> getCart(@PathVariable String cartId) {
        return ResponseEntity.ok(cartService.getCartByCartId(cartId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<List<CartDto>> getUserCarts(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCartsByUser(userId));
    }

    @PostMapping("/{cartId}/confirm")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<BookingDto> confirmCart(
            @PathVariable String cartId,
            @RequestParam(defaultValue = "ONLINE") String paymentMethod) {
        return new ResponseEntity<>(cartService.convertToBooking(cartId, paymentMethod), HttpStatus.CREATED);
    }
}
