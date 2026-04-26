import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { movieAPI } from '../services/api';
import MovieCard from '../components/MovieCard';
import HeroCarousel from '../components/HeroCarousel';
import './HomePage.css';

export default function HomePage() {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    movieAPI.getAll()
      .then(res => setMovies(res.data))
      .catch(err => console.error("Error fetching movies:", err))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="page home-page">
      {!loading && movies.length > 0 && (
        <HeroCarousel movies={movies} />
      )}

      <section className="section container">
        <div className="section-header">
          <h2 className="section-title">Recommended <span className="gradient-text">Movies</span></h2>
          <Link to="/movies" className="view-all-link">See all →</Link>
        </div>
        
        {loading ? (
          <div className="movies-grid">
            {[1, 2, 3, 4, 5].map(i => <div key={i} className="movie-card skeleton" style={{ height: '380px' }}></div>)}
          </div>
        ) : (
          <div className="movies-grid">
            {movies.slice(1).map(movie => (
              <MovieCard key={movie.id} movie={movie} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
