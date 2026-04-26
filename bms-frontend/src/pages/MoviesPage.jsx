import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { movieAPI } from '../services/api';
import MovieCard from '../components/MovieCard';
import './MoviesPage.css';

export default function MoviesPage() {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchParams] = useSearchParams();
  const initialSearch = searchParams.get('search') || '';
  const [searchTerm, setSearchTerm] = useState(initialSearch);

  useEffect(() => {
    fetchMovies(initialSearch);
  }, [initialSearch]);

  const fetchMovies = (query) => {
    setLoading(true);
    const request = query ? movieAPI.search(query) : movieAPI.getAll();
    request
      .then(res => setMovies(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
    if (e.target.value.length === 0 || e.target.value.length > 2) {
      // Basic debounce concept or just fetch on enter
      // Here we just fetch immediately if empty, otherwise wait for button
      if(e.target.value.length === 0) fetchMovies('');
    }
  };

  const executeSearch = (e) => {
    e.preventDefault();
    fetchMovies(searchTerm);
  };

  return (
    <div className="page movies-page container">
      <div className="page-header">
        <div>
          <h1 className="section-title">Explore <span className="gradient-text">Movies</span></h1>
          <p className="section-subtitle">Find your next cinematic experience</p>
        </div>
        
        <form className="movies-search-form" onSubmit={executeSearch}>
          <input 
            type="text" 
            placeholder="Search by title..." 
            className="input-field search-input"
            value={searchTerm}
            onChange={handleSearchChange}
          />
          <button type="submit" className="btn-primary search-btn">Search</button>
        </form>
      </div>

      {loading ? (
        <div className="movies-grid">
          {[...Array(8)].map((_, i) => <div key={i} className="movie-card skeleton" style={{ height: '380px' }}></div>)}
        </div>
      ) : movies.length > 0 ? (
        <div className="movies-grid">
          {movies.map(movie => <MovieCard key={movie.id} movie={movie} />)}
        </div>
      ) : (
        <div className="empty-state">
          <div className="empty-icon">🍿</div>
          <h3>No movies found</h3>
          <p>Try adjusting your search criteria</p>
          <button className="btn-outline mt-3" onClick={() => {setSearchTerm(''); fetchMovies('');}}>Clear Search</button>
        </div>
      )}
    </div>
  );
}
