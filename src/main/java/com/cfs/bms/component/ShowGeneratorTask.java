package com.cfs.bms.component;

import com.cfs.bms.dto.MovieDto;
import com.cfs.bms.dto.ScreenDto;
import com.cfs.bms.dto.ShowDto;
import com.cfs.bms.dto.TheaterDto;
import com.cfs.bms.model.Movie;
import com.cfs.bms.repository.MovieRepository;
import com.cfs.bms.service.ScreenService;
import com.cfs.bms.service.ShowService;
import com.cfs.bms.service.TheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ShowGeneratorTask {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheaterService theaterService;

    @Autowired
    private ScreenService screenService;

    @Autowired
    private ShowService showService;

    /**
     * Runs every day at midnight (00:00).
     * Generates shows for the 7th day from today, maintaining a 7-day rolling window.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void generateDailyShows() {
        System.out.println("🔄 Running daily background task: Generating shows for a week ahead...");
        List<Movie> movies = movieRepository.findAll();
        List<TheaterDto> theaters = theaterService.getAllTheaters();

        if (movies.isEmpty() || theaters.isEmpty()) {
            return;
        }

        // Generate shows for exactly 6 days from now (to keep a 7-day rolling window)
        LocalDateTime targetDate = LocalDateTime.now().plusDays(6).withHour(18).withMinute(0).withSecond(0).withNano(0);

        for (TheaterDto t : theaters) {
            List<ScreenDto> screens = screenService.getScreensByTheaterId(t.getId());
            for (int i = 0; i < screens.size(); i++) {
                ScreenDto s = screens.get(i);
                // Cycle through movies based on theater and screen index to add variety
                Movie m = movies.get((t.getId().intValue() + i) % movies.size());

                ShowDto show = new ShowDto();
                MovieDto movieDto = new MovieDto();
                movieDto.setId(m.getId());
                show.setMovie(movieDto);

                ScreenDto screenDto = new ScreenDto();
                screenDto.setId(s.getId());
                show.setScreen(screenDto);

                show.setStartTime(targetDate);
                show.setEndTime(targetDate.plusMinutes(m.getDurationMins()));

                try {
                    showService.creteShow(show);
                    System.out.println("✅ Scheduled show for " + m.getTitle() + " at " + targetDate.toLocalDate());
                } catch (Exception e) {
                    System.err.println("❌ Failed to schedule show: " + e.getMessage());
                }
            }
        }
    }
}
