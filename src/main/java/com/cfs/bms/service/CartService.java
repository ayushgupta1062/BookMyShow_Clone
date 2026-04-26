package com.cfs.bms.service;

import com.cfs.bms.dto.*;
import com.cfs.bms.exception.ResourceNotFoundException;
import com.cfs.bms.model.*;
import com.cfs.bms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    private static final int CART_EXPIRY_MINUTES = 15;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private BookingService bookingService;

    @Transactional
    public CartDto createCart(CartRequestDto request) {
        System.out.println("DEBUG: createCart called with userId: " + request.getUserId() + ", showId: " + request.getShowId());
        try {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

            Show show = showRepository.findById(request.getShowId())
                    .orElseThrow(() -> new ResourceNotFoundException("Show not found with ID: " + request.getShowId()));

            List<ShowSeat> seats = showSeatRepository.findAllById(request.getSeatIds());
            if (seats.isEmpty()) {
                throw new ResourceNotFoundException("No valid seats found for the given IDs");
            }

            double totalPrice = seats.stream()
                    .mapToDouble(s -> s.getPrice() != null ? s.getPrice() : 0.0)
                    .sum();

            Cart cart = new Cart();
            cart.setCartId(UUID.randomUUID().toString());
            cart.setUser(user);
            cart.setShow(show);
            cart.setSeatIds(request.getSeatIds());
            cart.setTotalPrice(totalPrice);
            cart.setStatus("ACTIVE");
            cart.setCreatedAt(LocalDateTime.now());
            cart.setExpiresAt(LocalDateTime.now().plusMinutes(CART_EXPIRY_MINUTES));

            Cart savedCart = cartRepository.save(cart);
            logger.info("✅ Cart created successfully: {} for user: {}", savedCart.getCartId(), user.getEmail());

            return mapToDto(savedCart, seats);
        } catch (Exception e) {
            logger.error("❌ ERROR CREATING CART: {}", e.getMessage(), e);
            throw e;
        }
    }

    public CartDto getCartByCartId(String cartId) {
        Cart cart = cartRepository.findByCartId(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found: " + cartId));

        // Check expiry
        if ("ACTIVE".equals(cart.getStatus()) && cart.getExpiresAt().isBefore(LocalDateTime.now())) {
            cart.setStatus("EXPIRED");
            cartRepository.save(cart);
        }

        List<ShowSeat> seats = showSeatRepository.findAllById(cart.getSeatIds());
        return mapToDto(cart, seats);
    }

    public List<CartDto> getCartsByUser(Long userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);
        return carts.stream()
                .map(cart -> {
                    List<ShowSeat> seats = showSeatRepository.findAllById(cart.getSeatIds());
                    return mapToDto(cart, seats);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingDto convertToBooking(String cartId, String paymentMethod) {
        Cart cart = cartRepository.findByCartId(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found: " + cartId));

        if (!"ACTIVE".equals(cart.getStatus())) {
            throw new IllegalStateException("Cart is not active. Status: " + cart.getStatus());
        }

        if (cart.getExpiresAt().isBefore(LocalDateTime.now())) {
            cart.setStatus("EXPIRED");
            cartRepository.save(cart);
            throw new IllegalStateException("Cart has expired. Please start a new booking.");
        }

        BookingRequestDto bookingRequest = new BookingRequestDto(
                cart.getUser().getId(),
                cart.getShow().getId(),
                cart.getSeatIds(),
                paymentMethod
        );

        BookingDto booking = bookingService.createBooking(bookingRequest);

        cart.setStatus("CONVERTED");
        cartRepository.save(cart);

        logger.info("Cart {} converted to booking: {}", cartId, booking.getBookingNumber());
        return booking;
    }

    private CartDto mapToDto(Cart cart, List<ShowSeat> seats) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setCartId(cart.getCartId());
        dto.setTotalPrice(cart.getTotalPrice());
        dto.setStatus(cart.getStatus());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setExpiresAt(cart.getExpiresAt());
        dto.setCartLink("/cart/" + cart.getCartId());

        // User
        UserDto userDto = new UserDto();
        userDto.setId(cart.getUser().getId());
        userDto.setName(cart.getUser().getName());
        userDto.setEmail(cart.getUser().getEmail());
        userDto.setPhoneNumber(cart.getUser().getPhoneNumber());
        dto.setUser(userDto);

        // Show (basic)
        ShowDto showDto = new ShowDto();
        showDto.setId(cart.getShow().getId());
        showDto.setStartTime(cart.getShow().getStartTime());
        showDto.setEndTime(cart.getShow().getEndTime());
        MovieDto movieDto = new MovieDto();
        movieDto.setId(cart.getShow().getMovie().getId());
        movieDto.setTitle(cart.getShow().getMovie().getTitle());
        movieDto.setPosterUrl(cart.getShow().getMovie().getPosterUrl());
        showDto.setMovie(movieDto);
        TheaterDto theaterDto = new TheaterDto();
        theaterDto.setId(cart.getShow().getScreen().getTheater().getId());
        theaterDto.setName(cart.getShow().getScreen().getTheater().getName());
        theaterDto.setCity(cart.getShow().getScreen().getTheater().getCity());
        ScreenDto screenDto = new ScreenDto();
        screenDto.setId(cart.getShow().getScreen().getId());
        screenDto.setName(cart.getShow().getScreen().getName());
        screenDto.setTheater(theaterDto);
        showDto.setScreen(screenDto);
        dto.setShow(showDto);

        // Seats
        List<ShowSeatDto> seatDtos = seats.stream().map(seat -> {
            ShowSeatDto seatDto = new ShowSeatDto();
            seatDto.setId(seat.getId());
            seatDto.setStatus(seat.getStatus());
            seatDto.setPrice(seat.getPrice());
            SeatDto baseSeat = new SeatDto();
            baseSeat.setId(seat.getSeat().getId());
            baseSeat.setSeatNumber(seat.getSeat().getSeatNumber());
            baseSeat.setSeatType(seat.getSeat().getSeatType());
            baseSeat.setBasePrice(seat.getSeat().getBasePrice());
            seatDto.setSeat(baseSeat);
            return seatDto;
        }).collect(Collectors.toList());
        dto.setSeats(seatDtos);

        return dto;
    }
}
