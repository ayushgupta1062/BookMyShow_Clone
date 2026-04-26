package com.cfs.bms.service;

import com.cfs.bms.dto.ScreenDto;
import com.cfs.bms.dto.TheaterDto;
import com.cfs.bms.model.Screen;
import com.cfs.bms.repository.ScreenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScreenService {

    @Autowired
    private ScreenRepository screenRepository;

    public List<ScreenDto> getScreensByTheaterId(Long theaterId) {
        // Need custom method in repo or just find all and filter
        List<Screen> screens = screenRepository.findAll().stream()
                .filter(s -> s.getTheater().getId().equals(theaterId))
                .collect(Collectors.toList());

        return screens.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private ScreenDto mapToDto(Screen screen) {
        ScreenDto dto = new ScreenDto();
        dto.setId(screen.getId());
        dto.setName(screen.getName());
        dto.setTotalSeats(screen.getTotalSeats());

        TheaterDto tDto = new TheaterDto();
        tDto.setId(screen.getTheater().getId());
        tDto.setName(screen.getTheater().getName());
        dto.setTheater(tDto);

        return dto;
    }
}
