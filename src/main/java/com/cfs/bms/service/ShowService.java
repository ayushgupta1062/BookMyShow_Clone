package com.cfs.bms.service;

import com.cfs.bms.dto.*;
import com.cfs.bms.exception.ResourceNotFoundException;
import com.cfs.bms.model.Movie;
import com.cfs.bms.model.Screen;
import com.cfs.bms.model.Show;
import com.cfs.bms.model.ShowSeat;
import com.cfs.bms.repository.MovieRepository;
import com.cfs.bms.repository.ScreenRepository;
import com.cfs.bms.repository.ShowRepository;
import com.cfs.bms.repository.ShowSeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ShowService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private SeatRecommendationService seatRecommendationService;


    public ShowDto creteShow(ShowDto showDto)
    {
        Show show=new Show();
        Movie movie=movieRepository.findById(showDto.getMovie().getId())
                .orElseThrow(()-> new ResourceNotFoundException("Movie Not Found"));

        Screen screen=screenRepository.findById(showDto.getScreen().getId())
                .orElseThrow(()-> new ResourceNotFoundException("Screen Not Found"));

        show.setMovie(movie);
        show.setScreen(screen);
        show.setStartTime(showDto.getStartTime());
        
        // Automatically calculate end time based on movie duration
        if (movie.getDurationMins() != null) {
            show.setEndTime(showDto.getStartTime().plusMinutes(movie.getDurationMins()));
        } else {
            show.setEndTime(showDto.getStartTime().plusHours(3)); // Default fallback
        }

        Show savedShow = showRepository.save(show);

        // Fetch the screen again with seats to ensure they are loaded (Lazy loading fix)
        Screen screenWithSeats = screenRepository.findById(screen.getId()).orElse(screen);
        
        // Crucial: Create ShowSeat records for every seat in this screen for the new show
        if (screenWithSeats.getSeats() != null && !screenWithSeats.getSeats().isEmpty()) {
            List<ShowSeat> showSeats = screenWithSeats.getSeats().stream().map(seat -> {
                ShowSeat showSeat = new ShowSeat();
                showSeat.setShow(savedShow);
                showSeat.setSeat(seat);
                showSeat.setStatus("AVAILABLE");
                showSeat.setPrice(seat.getBasePrice()); // Initial price from base seat
                return showSeat;
            }).collect(Collectors.toList());
            showSeatRepository.saveAll(showSeats);
        }

        List<ShowSeat> allSeats = showSeatRepository.findByShowId(savedShow.getId());
        return mapToDto(savedShow, allSeats);
    }

    public ShowDto getShowById(Long id)
    {
        Show show=showRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Show not found  with id: "+id));
        List<ShowSeat> allSeats = showSeatRepository.findByShowId(show.getId());
        
        // Auto-Repair: If show has no seats, generate them now
        if (allSeats.isEmpty()) {
            Screen screen = show.getScreen();
            if (screen.getSeats() != null && !screen.getSeats().isEmpty()) {
                List<ShowSeat> newShowSeats = screen.getSeats().stream().map(seat -> {
                    ShowSeat ss = new ShowSeat();
                    ss.setShow(show);
                    ss.setSeat(seat);
                    ss.setStatus("AVAILABLE");
                    ss.setPrice(seat.getBasePrice());
                    return ss;
                }).collect(Collectors.toList());
                allSeats = showSeatRepository.saveAll(newShowSeats);
            }
        }
        
        return mapToDto(show, allSeats);
    }

    public List<ShowDto> getAllShows()
    {
        List<Show> shows=showRepository.findAll();
        return shows.stream()
                .map(show -> {
                    List<ShowSeat> allSeats = showSeatRepository.findByShowId(show.getId());
                    return mapToDto(show, allSeats);
                })
                .collect(Collectors.toList());
    }

    public List<ShowDto> getShowsByMovie(Long movieId)
    {
        List<Show> shows=showRepository.findByMovieId(movieId);
        return shows.stream()
                .map(show -> {
                    List<ShowSeat> allSeats = showSeatRepository.findByShowId(show.getId());
                    return mapToDto(show, allSeats);
                })
                .collect(Collectors.toList());
    }

    public List<ShowDto> getShowsByMovieAndCity(Long movieId,String city)
    {
        List<Show> shows=showRepository.findByMovie_IdAndScreen_Theater_City(movieId,city);
        return shows.stream()
                .map(show -> {
                    List<ShowSeat> allSeats = showSeatRepository.findByShowId(show.getId());
                    return mapToDto(show, allSeats);
                })
                .collect(Collectors.toList());
    }

    public List<ShowDto> getShowsByDateRange(LocalDateTime startDate, LocalDateTime endDate)
    {
        List<Show> shows=showRepository.findByStartTimeBetween(startDate,endDate);
        return shows.stream()
                .map(show -> {
                    List<ShowSeat> allSeats = showSeatRepository.findByShowId(show.getId());
                    return mapToDto(show, allSeats);
                })
                .collect(Collectors.toList());
    }

    public com.cfs.bms.dto.SeatRecommendationDto getRecommendedSeats(Long showId, int count) {
        return seatRecommendationService.recommendSeats(showId, count);
    }

    public void deleteShow(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + id));
        // ShowSeat table has CASCADE or manual cleanup? 
        // Usually, manual or cascade in entity. Let's check Show.java
        showRepository.delete(show);
    }

    public ShowDto updateShowTime(Long id, LocalDateTime startTime) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + id));
        
        show.setStartTime(startTime);
        // Recalculate end time
        if (show.getMovie().getDurationMins() != null) {
            show.setEndTime(startTime.plusMinutes(show.getMovie().getDurationMins()));
        } else {
            show.setEndTime(startTime.plusHours(3));
        }
        
        Show updatedShow = showRepository.save(show);
        List<ShowSeat> allSeats = showSeatRepository.findByShowId(updatedShow.getId());
        return mapToDto(updatedShow, allSeats);
    }

    private ShowDto mapToDto(Show show, List<ShowSeat> availableSeats)
    {
        ShowDto showDto= new ShowDto();
        showDto.setId(show.getId());
        showDto.setStartTime(show.getStartTime());
        showDto.setEndTime(show.getEndTime());

        showDto.setMovie(new MovieDto(
                show.getMovie().getId(),
                show.getMovie().getTitle(),
                show.getMovie().getDescription(),
                show.getMovie().getLanguage(),
                show.getMovie().getGenre(),
                show.getMovie().getDurationMins(),
                show.getMovie().getReleaseDate(),
                show.getMovie().getPosterUrl(),
                show.getMovie().getBackdropUrl()
        ));

        TheaterDto theaterDto=new TheaterDto(
                show.getScreen().getTheater().getId(),
                show.getScreen().getTheater().getName(),
                show.getScreen().getTheater().getAddress(),
                show.getScreen().getTheater().getCity(),
                show.getScreen().getTheater().getTotalScreens()
        );

        showDto.setScreen(new ScreenDto(
                show.getScreen().getId(),
                show.getScreen().getName(),
                show.getScreen().getTotalSeats(),
                theaterDto
        ));

        List<ShowSeatDto> seatDtos= availableSeats.stream()
                .map(seat->{
                    ShowSeatDto seatDto=new ShowSeatDto();
                    seatDto.setId(seat.getId());
                    seatDto.setStatus(seat.getStatus());
                    seatDto.setPrice(seat.getPrice());

                    SeatDto baseSeatDto=new SeatDto();
                    baseSeatDto.setId(seat.getSeat().getId());
                    baseSeatDto.setSeatNumber(seat.getSeat().getSeatNumber());
                    baseSeatDto.setSeatType(seat.getSeat().getSeatType());
                    baseSeatDto.setBasePrice(seat.getSeat().getBasePrice());
                    seatDto.setSeat(baseSeatDto);
                    return seatDto;
                })
                .collect(Collectors.toList());

        showDto.setAvailableSeats(seatDtos);
        return showDto;
    }
}