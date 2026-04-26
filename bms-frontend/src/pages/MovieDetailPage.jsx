import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { movieAPI, showAPI } from '../services/api';
import './MovieDetailPage.css';

export default function MovieDetailPage() {
  const { id } = useParams();
  const [movie, setMovie] = useState(null);
  const [shows, setShows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedDate, setSelectedDate] = useState('');

  useEffect(() => {
    Promise.all([
      movieAPI.getById(id),
      showAPI.getByMovie(id)
    ])
    .then(([movieRes, showsRes]) => {
      setMovie(movieRes.data);
      setShows(showsRes.data);
      if (showsRes.data.length > 0) {
        // Set default date to the first available show date
        const dates = [...new Set(showsRes.data.map(s => s.startTime.split('T')[0]))].sort();
        setSelectedDate(dates[0]);
      }
    })
    .catch(err => console.error(err))
    .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="page container"><div className="skeleton" style={{height:'60vh', borderRadius:'16px'}}></div></div>;
  if (!movie) return <div className="page container"><h2>Movie not found</h2></div>;

  const availableDates = [...new Set(shows.map(s => s.startTime.split('T')[0]))].sort();
  const filteredShows = shows.filter(s => s.startTime.startsWith(selectedDate));

  // Group shows by theater
  const showsByTheater = filteredShows.reduce((acc, show) => {
    const tName = show.screen.theater.name;
    if (!acc[tName]) acc[tName] = [];
    acc[tName].push(show);
    return acc;
  }, {});

  return (
    <div className="page movie-detail-page">
      <div className="movie-banner">
        <div className="banner-bg" style={{backgroundImage: `url(${movie.posterUrl})`}}></div>
        <div className="container banner-content">
          <img src={movie.posterUrl} alt={movie.title} className="detail-poster" />
          <div className="detail-info">
            <h1 className="detail-title">{movie.title}</h1>
            <div className="detail-meta">
              {movie.durationMins && <span className="badge badge-gold">{movie.durationMins} min</span>}
              {movie.language && <span className="badge badge-red">{movie.language}</span>}
              {movie.genre && <span>{movie.genre}</span>}
              {movie.releaseDate && <span>📅 {movie.releaseDate}</span>}
            </div>
            <p className="detail-desc">{movie.description}</p>
          </div>
        </div>
      </div>

      <div className="container shows-section">
        <h2 className="section-title mb-4">Book <span className="gradient-text">Tickets</span></h2>
        
        {availableDates.length > 0 ? (
          <>
            <div className="date-selector">
              {availableDates.map(date => (
                <button 
                  key={date} 
                  className={`date-btn ${selectedDate === date ? 'active' : ''}`}
                  onClick={() => setSelectedDate(date)}
                >
                  <div className="date-month">{new Date(date).toLocaleString('default', { month: 'short' })}</div>
                  <div className="date-day">{new Date(date).getDate()}</div>
                </button>
              ))}
            </div>

            <div className="theater-list">
              {Object.entries(showsByTheater).map(([theaterName, tShows]) => (
                <div key={theaterName} className="theater-item glass-card">
                  <h3 className="theater-name">🏛️ {theaterName}</h3>
                  <div className="show-times">
                    {tShows.map(show => {
                      const time = new Date(show.startTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
                      return (
                        <Link key={show.id} to={`/shows/${show.id}`} className="time-btn">
                          {time}
                        </Link>
                      );
                    })}
                  </div>
                </div>
              ))}
              {Object.keys(showsByTheater).length === 0 && (
                <p>No shows available for this date.</p>
              )}
            </div>
          </>
        ) : (
          <div className="empty-state">
            <p>No shows are currently scheduled for this movie.</p>
          </div>
        )}
      </div>
    </div>
  );
}
