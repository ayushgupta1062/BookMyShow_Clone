package com.cfs.bms.controller;

import com.cfs.bms.dto.ScreenDto;
import com.cfs.bms.service.ScreenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screens")
public class ScreenController {

    @Autowired
    private ScreenService screenService;

    @GetMapping("/theater/{theaterId}")
    public ResponseEntity<List<ScreenDto>> getScreensByTheater(@PathVariable Long theaterId) {
        return ResponseEntity.ok(screenService.getScreensByTheaterId(theaterId));
    }
}
