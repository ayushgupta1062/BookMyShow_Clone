package com.cfs.bms.component;

import com.cfs.bms.dto.MovieDto;
import com.cfs.bms.dto.ScreenDto;
import com.cfs.bms.dto.ShowDto;
import com.cfs.bms.dto.TheaterDto;
import com.cfs.bms.model.Movie;
import com.cfs.bms.model.User;
import com.cfs.bms.repository.MovieRepository;
import com.cfs.bms.repository.UserRepository;
import com.cfs.bms.service.ScreenService;
import com.cfs.bms.service.ShowService;
import com.cfs.bms.service.TheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheaterService theaterService;

    @Autowired
    private ShowService showService;

    @Autowired
    private ScreenService screenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String[][] movieData = {
                { "Dune: Part Two", "Sci-Fi, Adventure", "English", "166", "2024-03-01",
                        "https://tse1.mm.bing.net/th/id/OIP.eMoRWdy4PDxagmLcN0w7YwHaLH?w=960&h=1440&rs=1&pid=ImgDetMain&o=7&rm=3",
                        "https://images.plex.tv/photo?size=large-1280&url=https:%2F%2Fmetadata-static.plex.tv%2F5%2Fgracenote%2F56b43b2dd7bc2b87c6259672bab47dfa.jpg",
                        "Paul Atreides unites with Chani and the Fremen while on a warpath of revenge against the conspirators who destroyed his family." },
                { "Oppenheimer", "Biography, Drama", "English", "180", "2023-07-21",
                        "https://image.tmdb.org/t/p/original/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                        "https://image.tmdb.org/t/p/original/nb3uS7z9vNfj99vYmt79vGPv69m.jpg",
                        "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb." },
                { "Jawan", "Action, Thriller", "Hindi", "169", "2023-09-07",
                        "https://occ-0-3011-116.1.nflxso.net/dnm/api/v6/E8vDc_W8CLv7-yMQu8KMEC7Rrr8/AAAABQvKH5_Hp6dtGbwlU0QTaFS0zviQ3eXWacIYTrgArimrDGZUtoPYRAWxTHoYCe0_uiTdjDCbHFBb-jn_36vAxJUTyAuJTIV47usl.jpg?r=a78",
                        "https://occ-0-3011-116.1.nflxso.net/dnm/api/v6/E8vDc_W8CLv7-yMQu8KMEC7Rrr8/AAAABQvKH5_Hp6dtGbwlU0QTaFS0zviQ3eXWacIYTrgArimrDGZUtoPYRAWxTHoYCe0_uiTdjDCbHFBb-jn_36vAxJUTyAuJTIV47usl.jpg?r=a78",
                        "A high-octane action thriller which outlines the emotional journey of a man who is set to rectify the wrongs in the society." },
                { "Animal", "Action, Crime", "Hindi", "201", "2023-12-01",
                        "https://wallpaperaccess.com/full/12247313.jpg",
                        "https://image.tmdb.org/t/p/original/hr9rjR3J0xBBKmlJ4n3gvdnACd5.jpg",
                        "A son's obsessive love for his father leads to massive bloodshed and turmoil." },
                { "Kalki 2898 AD", "Sci-Fi, Action", "Telugu", "181", "2024-06-27",
                        "https://m.media-amazon.com/images/S/pv-target-images/0e8c2a540ecdd6830315a6a1154460e3d047da9349d3b201b5cb355ed077dd04.jpg",
                        "https://occ-0-3012-114.1.nflxso.net/dnm/api/v6/6AYY37jfdO6hpXcMjf9Yu5cnmO0/AAAABWTNk4PPYuKp5vgKJlhm2VraP3HCK0j8_YSJsme1Yf1qTJikucMD4ckSOJEtHJaHDT-4OxFyWok6l0m8hGcYYuMHWGrHwAmch6eT.jpg?r=f9c",
                        "A modern-day avatar of Vishnu, a Hindu god, who is believed to have descended to earth to protect the world from evil forces." },
                { "Salaar: Part 1 - Ceasefire", "Action, Thriller", "Telugu", "175", "2023-12-22",
                        "https://occ-0-3011-116.1.nflxso.net/dnm/api/v6/E8vDc_W8CLv7-yMQu8KMEC7Rrr8/AAAABWpJcd5ZjXBIpedLgwpj1JP-u_rAdjlr4Nn0M0e9yBn1S8vYfS2fP8l1Tg0jxjWJ7TRWOt2Cs4hovWr9ecrm2YpLQsFOuGyaVvfV.jpg?r=ca1",
                        "https://occ-0-3011-116.1.nflxso.net/dnm/api/v6/E8vDc_W8CLv7-yMQu8KMEC7Rrr8/AAAABWpJcd5ZjXBIpedLgwpj1JP-u_rAdjlr4Nn0M0e9yBn1S8vYfS2fP8l1Tg0jxjWJ7TRWOt2Cs4hovWr9ecrm2YpLQsFOuGyaVvfV.jpg?r=ca1",
                        "A gang leader makes a promise to his dying friend and takes on the other criminal gangs." }
        };

        // Always sync poster and backdrop URLs from code to DB
        for (String[] md : movieData) {
            movieRepository.findByTitle(md[0]).ifPresent(m -> {
                m.setPosterUrl(md[5]);
                m.setBackdropUrl(md[6]);
                movieRepository.save(m);
            });
        }

        // Only do full initial seed if DB is empty
        if (movieRepository.count() == 0) {
            for (String[] md : movieData) {
                Movie m = new Movie();
                m.setTitle(md[0]);
                m.setGenre(md[1]);
                m.setLanguage(md[2]);
                m.setDurationMins(Integer.parseInt(md[3]));
                // Note: releaseDate is string in entity
                m.setReleaseDate(md[4]);
                m.setPosterUrl(md[5]);
                m.setBackdropUrl(md[6]);
                m.setDescription(md[7]);
                movieRepository.save(m);
            }
            System.out.println("✅ Movies seeded.");

            // 3. Seed Theaters & Shows (only if movies were just seeded to avoid
            // duplication)
            TheaterDto t1 = new TheaterDto();
            t1.setName("PVR ICON Java");
            t1.setCity("Mumbai");
            t1.setAddress("Phoenix Palladium, Lower Parel");
            t1.setTotalScreens(3);
            t1 = theaterService.createTheater(t1);

            TheaterDto t2 = new TheaterDto();
            t2.setName("INOX Laserplex Java");
            t2.setCity("Delhi");
            t2.setAddress("Nehru Place");
            t2.setTotalScreens(2);
            t2 = theaterService.createTheater(t2);

            System.out.println("✅ Theaters seeded.");

            // Add Shows
            List<Movie> movies = movieRepository.findAll();
            List<TheaterDto> theaters = List.of(t1, t2);

            for (TheaterDto t : theaters) {
                List<ScreenDto> screens = screenService.getScreensByTheaterId(t.getId());
                for (int i = 0; i < screens.size(); i++) {
                    ScreenDto s = screens.get(i);
                    Movie m = movies.get((t.getId().intValue() + i) % movies.size());

                    // Seed shows for the next 7 days
                    for (int day = 0; day < 7; day++) {
                        ShowDto show = new ShowDto();
                        MovieDto movieDto = new MovieDto();
                        movieDto.setId(m.getId());
                        show.setMovie(movieDto);

                        ScreenDto screenDto = new ScreenDto();
                        screenDto.setId(s.getId());
                        show.setScreen(screenDto);

                        show.setStartTime(
                                LocalDateTime.now().plusDays(day).withHour(18).withMinute(0).withSecond(0).withNano(0));
                        show.setEndTime(show.getStartTime().plusMinutes(m.getDurationMins()));

                        showService.creteShow(show);
                    }
                }
            }
            System.out.println("✅ Shows seeded.");
        }
    }
}
