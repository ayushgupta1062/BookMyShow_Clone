import { Link } from 'react-router-dom';
import './MovieCard.css';

export default function MovieCard({ movie }) {
  return (
    <Link to={`/movies/${movie.id}`} className="movie-card">
      <div className="movie-poster-wrap">
        {movie.posterUrl
          ? <img src={movie.posterUrl} alt={movie.title} className="movie-poster" />
          : <div className="movie-poster-placeholder">🎬</div>
        }
        <div className="movie-overlay">
          <span className="view-btn">View Showtimes →</span>
        </div>
        {movie.genre && <span className="movie-genre-tag">{movie.genre}</span>}
      </div>
      <div className="movie-info">
        <h3 className="movie-title">{movie.title}</h3>
        <div className="movie-meta">
          {movie.language && <span className="meta-tag">{movie.language}</span>}
          {movie.durationMins && <span className="meta-tag">⏱ {movie.durationMins}m</span>}
        </div>
        {movie.description && (
          <p className="movie-desc">{movie.description.slice(0, 80)}{movie.description.length > 80 ? '...' : ''}</p>
        )}
      </div>
    </Link>
  );
}
