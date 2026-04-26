import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import './HeroCarousel.css';

export default function HeroCarousel({ movies }) {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isTransitioning, setIsTransitioning] = useState(false);

  const nextSlide = useCallback(() => {
    if (movies.length === 0) return;
    setIsTransitioning(true);
    setTimeout(() => {
      setCurrentIndex((prev) => (prev + 1) % movies.length);
      setIsTransitioning(false);
    }, 500);
  }, [movies.length]);

  const prevSlide = () => {
    if (movies.length === 0) return;
    setIsTransitioning(true);
    setTimeout(() => {
      setCurrentIndex((prev) => (prev - 1 + movies.length) % movies.length);
      setIsTransitioning(false);
    }, 500);
  };

  useEffect(() => {
    if (movies.length <= 1) return;
    const interval = setInterval(nextSlide, 5000);
    return () => clearInterval(interval);
  }, [nextSlide, movies.length]);

  if (!movies || movies.length === 0) return null;

  const currentMovie = movies[currentIndex];

  return (
    <div className="hero-carousel">
      <div className={`hero-slide ${isTransitioning ? 'fade-out' : 'fade-in'}`}>
        <div className="hero-bg">
          {currentMovie.backdropUrl ? (
            <img src={currentMovie.backdropUrl} alt={currentMovie.title} />
          ) : currentMovie.posterUrl ? (
            <img src={currentMovie.posterUrl} alt={currentMovie.title} />
          ) : (
            <div className="hero-bg-placeholder" />
          )}
          <div className="hero-overlay"></div>
        </div>
        
        <div className="container hero-content">
          <div className="hero-info-wrapper">
            <span className="badge badge-gold mb-3">Now Showing</span>
            <h1 className="hero-title">{currentMovie.title}</h1>
            <div className="movie-meta mb-4">
              {currentMovie.language && <span className="meta-tag">{currentMovie.language}</span>}
              {currentMovie.genre && <span className="meta-tag">{currentMovie.genre}</span>}
              {currentMovie.durationMins && <span className="meta-tag">{currentMovie.durationMins}m</span>}
            </div>
            <p className="hero-desc">{currentMovie.description}</p>
            <div className="hero-actions">
              <Link to={`/movies/${currentMovie.id}`} className="btn-primary">Book Tickets</Link>
              <Link to="/movies" className="btn-outline glass-btn">View All Movies</Link>
            </div>
          </div>
        </div>
      </div>

      <div className="carousel-controls">
        <button className="carousel-arrow prev" onClick={prevSlide}>&lsaquo;</button>
        <button className="carousel-arrow next" onClick={nextSlide}>&rsaquo;</button>
      </div>

      <div className="carousel-dots">
        {movies.map((_, index) => (
          <button
            key={index}
            className={`dot ${index === currentIndex ? 'active' : ''}`}
            onClick={() => {
              setIsTransitioning(true);
              setTimeout(() => {
                setCurrentIndex(index);
                setIsTransitioning(false);
              }, 500);
            }}
          ></button>
        ))}
      </div>
    </div>
  );
}
