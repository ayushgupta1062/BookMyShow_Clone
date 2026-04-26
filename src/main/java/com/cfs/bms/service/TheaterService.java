package com.cfs.bms.service;

import com.cfs.bms.dto.TheaterDto;
import com.cfs.bms.exception.ResourceNotFoundException;
import com.cfs.bms.model.Theater;
import com.cfs.bms.repository.TheaterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.cfs.bms.model.Screen;
import com.cfs.bms.model.Seat;

@Service
public class TheaterService {

    private static final Logger logger = LoggerFactory.getLogger(TheaterService.class);

    @Autowired
    private TheaterRepository theaterRepository;

    public TheaterDto createTheater(TheaterDto theaterDto) {
        Theater theater = mapToEntity(theaterDto);
        
        if (theater.getTotalScreens() != null && theater.getTotalScreens() > 0) {
            List<Screen> screens = new ArrayList<>();
            for (int i = 1; i <= theater.getTotalScreens(); i++) {
                Screen screen = new Screen();
                screen.setName("Screen " + i);
                screen.setTotalSeats(50);
                screen.setTheater(theater);

                List<Seat> seats = new ArrayList<>();
                String[] rows = {"A", "B", "C", "D", "E"};
                for (String row : rows) {
                    for (int num = 1; num <= 10; num++) {
                        Seat seat = new Seat();
                        seat.setSeatNumber(row + num);
                        seat.setScreen(screen);
                        if (row.equals("A")) {
                            seat.setSeatType("SILVER");
                            seat.setBasePrice(150.0);
                        } else if (row.equals("E")) {
                            seat.setSeatType("PLATINUM");
                            seat.setBasePrice(300.0);
                        } else {
                            seat.setSeatType("GOLD");
                            seat.setBasePrice(200.0);
                        }
                        seats.add(seat);
                    }
                }
                screen.setSeats(seats);
                screens.add(screen);
            }
            theater.setScreens(screens);
        }

        Theater savedTheater = theaterRepository.save(theater);
        logger.info("Theater created: {} with {} screens", savedTheater.getName(), savedTheater.getTotalScreens());
        return mapToDto(savedTheater);
    }

    public TheaterDto getTheaterById(Long id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));
        return mapToDto(theater);
    }

    public List<TheaterDto> getAllTheaters() {
        List<Theater> theaters = theaterRepository.findAll();
        return theaters.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<TheaterDto> getTheatersByCity(String city) {
        List<Theater> theaters = theaterRepository.findByCity(city);
        return theaters.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public TheaterDto updateTheater(Long id, TheaterDto theaterDto) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));
        theater.setName(theaterDto.getName());
        theater.setAddress(theaterDto.getAddress());
        theater.setCity(theaterDto.getCity());
        theater.setTotalScreens(theaterDto.getTotalScreens());
        Theater updatedTheater = theaterRepository.save(theater);
        logger.info("Theater updated: {}", updatedTheater.getId());
        return mapToDto(updatedTheater);
    }

    public void deleteTheater(Long id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));
        theaterRepository.delete(theater);
        logger.info("Theater deleted: {}", id);
    }

    public TheaterDto mapToDto(Theater theater) {
        TheaterDto theaterDto = new TheaterDto();
        theaterDto.setId(theater.getId());
        theaterDto.setName(theater.getName());
        theaterDto.setCity(theater.getCity());
        theaterDto.setAddress(theater.getAddress());
        theaterDto.setTotalScreens(theater.getTotalScreens());
        return theaterDto;
    }

    public Theater mapToEntity(TheaterDto theaterDto) {
        Theater theater = new Theater();
        theater.setName(theaterDto.getName());
        theater.setAddress(theaterDto.getAddress());
        theater.setCity(theaterDto.getCity());
        theater.setTotalScreens(theaterDto.getTotalScreens());
        return theater;
    }
}