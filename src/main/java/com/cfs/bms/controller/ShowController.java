package com.cfs.bms.controller;

import com.cfs.bms.dto.ShowDto;
import com.cfs.bms.service.ShowService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shows")
public class ShowController {

    @Autowired
    private ShowService showService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShowDto> createShow(@Valid @RequestBody ShowDto showDto) {
        return new ResponseEntity<>(showService.creteShow(showDto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ShowDto>> getAllShows() {
        return ResponseEntity.ok(showService.getAllShows());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowDto> getShowById(@PathVariable Long id) {
        return ResponseEntity.ok(showService.getShowById(id));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowDto>> getShowsByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(showService.getShowsByMovie(movieId));
    }

    @GetMapping("/movie/{movieId}/city/{city}")
    public ResponseEntity<List<ShowDto>> getShowsByMovieAndCity(@PathVariable Long movieId,
                                                                 @PathVariable String city) {
        return ResponseEntity.ok(showService.getShowsByMovieAndCity(movieId, city));
    }

    @GetMapping("/{id}/recommend-seats")
    public ResponseEntity<?> getRecommendedSeats(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "2") int count) {
        return ResponseEntity.ok(showService.getRecommendedSeats(id, count));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteShow(@PathVariable Long id) {
        showService.deleteShow(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/time")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShowDto> updateShowTime(@PathVariable Long id, @RequestBody java.time.LocalDateTime startTime) {
        return ResponseEntity.ok(showService.updateShowTime(id, startTime));
    }
}
