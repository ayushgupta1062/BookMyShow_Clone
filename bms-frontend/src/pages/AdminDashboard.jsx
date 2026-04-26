import { useState, useEffect } from 'react';
import { movieAPI, theaterAPI, showAPI, screenAPI } from '../services/api';
import './AdminDashboard.css';

export default function AdminDashboard() {
  const [activeTab, setActiveTab] = useState('movies');

  // Movie state
  const [movieData, setMovieData] = useState({ title: '', genre: '', language: '', durationMins: '', description: '', posterUrl: '', backdropUrl: '', releaseDate: '' });

  // Theater state
  const [theaterData, setTheaterData] = useState({ name: '', city: '', address: '', totalScreens: '' });

  // Show state
  const [showData, setShowData] = useState({ movieId: '', theaterId: '', screenId: '', startTime: '', endTime: '' });
  const [moviesList, setMoviesList] = useState([]);
  const [theatersList, setTheatersList] = useState([]);
  const [screensList, setScreensList] = useState([]);

  // UI state
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  
  // Show Management state
  const [managingMovie, setManagingMovie] = useState(null);
  const [showsForMovie, setShowsForMovie] = useState([]);
  const [editingShowId, setEditingShowId] = useState(null);
  const [editShowTime, setEditShowTime] = useState('');

  useEffect(() => {
    if (activeTab === 'shows' || activeTab === 'manage') {
      movieAPI.getAll().then(res => setMoviesList(res.data)).catch(console.error);
      if (activeTab === 'shows') theaterAPI.getAll().then(res => setTheatersList(res.data)).catch(console.error);
    }
  }, [activeTab]);

  useEffect(() => {
    if (showData.theaterId) {
      screenAPI.getByTheater(showData.theaterId)
        .then(res => setScreensList(res.data))
        .catch(console.error);
    } else {
      setScreensList([]);
    }
  }, [showData.theaterId]);

  const showMsg = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 4000);
  };

  const handleDeleteMovie = (id) => {
    if (window.confirm('Are you sure you want to delete this movie? All associated shows and bookings will be deleted.')) {
      movieAPI.delete(id)
        .then(() => {
          showMsg('success', 'Movie deleted successfully!');
          setMoviesList(moviesList.filter(m => m.id !== id));
        })
        .catch(err => showMsg('error', err.response?.data?.message || 'Error deleting movie.'));
    }
  };

  const handleManageShows = (movie) => {
    setManagingMovie(movie);
    showAPI.getByMovie(movie.id)
      .then(res => setShowsForMovie(res.data))
      .catch(err => showMsg('error', 'Error fetching shows'));
  };

  const handleDeleteShow = (showId) => {
    if (window.confirm('Delete this show? All bookings for this show will be cancelled.')) {
      showAPI.delete(showId)
        .then(() => {
          showMsg('success', 'Show deleted successfully!');
          setShowsForMovie(showsForMovie.filter(s => s.id !== showId));
        })
        .catch(err => showMsg('error', 'Error deleting show.'));
    }
  };

  const handleUpdateTime = (showId) => {
    if (!editShowTime) return;
    setLoading(true);
    showAPI.updateTime(showId, editShowTime)
      .then(res => {
        showMsg('success', 'Show time updated!');
        setShowsForMovie(showsForMovie.map(s => s.id === showId ? res.data : s));
        setEditingShowId(null);
      })
      .catch(err => showMsg('error', 'Error updating time.'))
      .finally(() => setLoading(false));
  };

  const handleMovieSubmit = (e) => {
    e.preventDefault();
    setLoading(true);
    movieAPI.create(movieData)
      .then(() => {
        showMsg('success', 'Movie added successfully!');
        setMovieData({ title: '', genre: '', language: '', durationMins: '', description: '', posterUrl: '', backdropUrl: '', releaseDate: '' });
      })
      .catch(err => showMsg('error', err.response?.data?.message || 'Error adding movie'))
      .finally(() => setLoading(false));
  };

  const handleTheaterSubmit = (e) => {
    e.preventDefault();
    setLoading(true);
    theaterAPI.create(theaterData)
      .then(() => {
        showMsg('success', 'Theater added successfully! Screens & Seats generated.');
        setTheaterData({ name: '', city: '', address: '', totalScreens: '' });
      })
      .catch(err => showMsg('error', err.response?.data?.message || 'Error adding theater'))
      .finally(() => setLoading(false));
  };

  const handleShowSubmit = (e) => {
    e.preventDefault();
    setLoading(true);
    
    const payload = {
      startTime: showData.startTime,
      endTime: showData.endTime,
      movie: { id: showData.movieId },
      screen: { id: showData.screenId }
    };

    showAPI.create(payload)
      .then(() => {
        showMsg('success', 'Show scheduled successfully!');
        setShowData({ movieId: '', theaterId: '', screenId: '', startTime: '', endTime: '' });
      })
      .catch(err => showMsg('error', err.response?.data?.message || 'Error adding show'))
      .finally(() => setLoading(false));
  };

  return (
    <div className="page admin-dashboard container">
      <h2 className="section-title mb-4">Admin <span className="gradient-text">Portal</span></h2>

      <div className="admin-tabs">
        <button className={`admin-tab ${activeTab === 'movies' ? 'active' : ''}`} onClick={() => setActiveTab('movies')}>Add Movie</button>
        <button className={`admin-tab ${activeTab === 'theaters' ? 'active' : ''}`} onClick={() => setActiveTab('theaters')}>Add Theater</button>
        <button className={`admin-tab ${activeTab === 'shows' ? 'active' : ''}`} onClick={() => setActiveTab('shows')}>Schedule Show</button>
        <button className={`admin-tab ${activeTab === 'manage' ? 'active' : ''}`} onClick={() => setActiveTab('manage')}>Manage Movies</button>
      </div>

      {message.text && (
        <div className={`admin-msg ${message.type}`}>
          {message.text}
        </div>
      )}

      <div className="admin-content glass-card">
        {activeTab === 'movies' && (
          <form onSubmit={handleMovieSubmit} className="admin-form">
            <h3 className="mb-4">Add New Movie</h3>
            <div className="form-grid">
              <div className="form-group">
                <label className="input-label">Title</label>
                <input className="input-field" required value={movieData.title} onChange={e => setMovieData({...movieData, title: e.target.value})} />
              </div>
              <div className="form-group">
                <label className="input-label">Genre</label>
                <input className="input-field" required value={movieData.genre} onChange={e => setMovieData({...movieData, genre: e.target.value})} placeholder="e.g. Action, Sci-Fi" />
              </div>
              <div className="form-group">
                <label className="input-label">Language</label>
                <input className="input-field" required value={movieData.language} onChange={e => setMovieData({...movieData, language: e.target.value})} />
              </div>
              <div className="form-group">
                <label className="input-label">Duration (Mins)</label>
                <input type="number" className="input-field" required value={movieData.durationMins} onChange={e => setMovieData({...movieData, durationMins: e.target.value})} />
              </div>
              <div className="form-group">
                <label className="input-label">Release Date</label>
                <input type="date" className="input-field" required value={movieData.releaseDate} onChange={e => setMovieData({...movieData, releaseDate: e.target.value})} />
              </div>
              <div className="form-group">
                <label className="input-label">Poster URL (Vertical/Card)</label>
                <input type="url" className="input-field" required value={movieData.posterUrl} onChange={e => setMovieData({...movieData, posterUrl: e.target.value})} placeholder="https://..." />
              </div>
              <div className="form-group">
                <label className="input-label">Backdrop URL (Horizontal/Banner)</label>
                <input type="url" className="input-field" required value={movieData.backdropUrl} onChange={e => setMovieData({...movieData, backdropUrl: e.target.value})} placeholder="https://..." />
              </div>
              <div className="form-group full-width">
                <label className="input-label">Description</label>
                <textarea className="input-field" rows="4" required value={movieData.description} onChange={e => setMovieData({...movieData, description: e.target.value})}></textarea>
              </div>
            </div>
            <button type="submit" className="btn-primary mt-4" disabled={loading}>{loading ? 'Adding...' : 'Save Movie'}</button>
          </form>
        )}

        {activeTab === 'theaters' && (
          <form onSubmit={handleTheaterSubmit} className="admin-form">
            <h3 className="mb-4">Add New Theater</h3>
            <div className="form-grid">
              <div className="form-group">
                <label className="input-label">Theater Name</label>
                <input className="input-field" required value={theaterData.name} onChange={e => setTheaterData({...theaterData, name: e.target.value})} placeholder="e.g. INOX, PVR" />
              </div>
              <div className="form-group">
                <label className="input-label">City</label>
                <input className="input-field" required value={theaterData.city} onChange={e => setTheaterData({...theaterData, city: e.target.value})} />
              </div>
              <div className="form-group">
                <label className="input-label">Total Screens</label>
                <input type="number" className="input-field" required min="1" value={theaterData.totalScreens} onChange={e => setTheaterData({...theaterData, totalScreens: e.target.value})} />
              </div>
              <div className="form-group full-width">
                <label className="input-label">Address</label>
                <input className="input-field" required value={theaterData.address} onChange={e => setTheaterData({...theaterData, address: e.target.value})} />
              </div>
            </div>
            <button type="submit" className="btn-primary mt-4" disabled={loading}>{loading ? 'Adding...' : 'Save Theater'}</button>
          </form>
        )}

        {activeTab === 'shows' && (
          <form onSubmit={handleShowSubmit} className="admin-form">
            <h3 className="mb-4">Schedule a Show</h3>
            <div className="form-grid">
              <div className="form-group">
                <label className="input-label">Select Movie</label>
                <select className="input-field" required value={showData.movieId} onChange={e => setShowData({...showData, movieId: e.target.value})}>
                  <option value="">-- Choose Movie --</option>
                  {moviesList.map(m => <option key={m.id} value={m.id}>{m.title}</option>)}
                </select>
              </div>
              
              <div className="form-group">
                <label className="input-label">Select Theater</label>
                <select className="input-field" required value={showData.theaterId} onChange={e => setShowData({...showData, theaterId: e.target.value})}>
                  <option value="">-- Choose Theater --</option>
                  {theatersList.map(t => <option key={t.id} value={t.id}>{t.name} ({t.city})</option>)}
                </select>
              </div>

              <div className="form-group">
                <label className="input-label">Select Screen</label>
                <select className="input-field" required value={showData.screenId} onChange={e => setShowData({...showData, screenId: e.target.value})} disabled={!showData.theaterId}>
                  <option value="">-- Choose Screen --</option>
                  {screensList.map(s => <option key={s.id} value={s.id}>{s.name} ({s.totalSeats} seats)</option>)}
                </select>
              </div>

              <div className="form-group"></div>

              <div className="form-group">
                <label className="input-label">Start Time</label>
                <input type="datetime-local" className="input-field" required value={showData.startTime} onChange={e => setShowData({...showData, startTime: e.target.value})} />
              </div>
            </div>
            <button type="submit" className="btn-primary mt-4" disabled={loading}>{loading ? 'Scheduling...' : 'Schedule Show'}</button>
          </form>
        )}

        {activeTab === 'manage' && (
          <div className="admin-form">
            <h3 className="mb-4">Manage Movies & Shows</h3>
            
            {!managingMovie ? (
              <div className="movie-list">
                {moviesList.length === 0 ? <p>No movies found.</p> : moviesList.map(movie => (
                  <div key={movie.id} className="admin-movie-item" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '15px', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
                      <img src={movie.posterUrl} alt={movie.title} style={{ width: '50px', height: '75px', objectFit: 'cover', borderRadius: '4px' }} />
                      <div>
                        <h4 style={{ margin: 0 }}>{movie.title}</h4>
                        <small style={{ color: 'var(--text-muted)' }}>{movie.genre} | {movie.language}</small>
                      </div>
                    </div>
                    <div style={{ display: 'flex', gap: '10px' }}>
                      <button className="btn-secondary" style={{ padding: '8px 16px', fontSize: '0.85rem' }} onClick={() => handleManageShows(movie)}>Manage Shows</button>
                      <button className="btn-primary" style={{ background: 'var(--accent)', padding: '8px 16px', fontSize: '0.85rem' }} onClick={() => handleDeleteMovie(movie.id)}>Delete Movie</button>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="manage-shows-container">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', borderBottom: '1px solid var(--border)', paddingBottom: '10px' }}>
                  <h4 style={{ margin: 0 }}>Shows for: <span className="gradient-text">{managingMovie.title}</span></h4>
                  <button className="btn-secondary" onClick={() => setManagingMovie(null)}>← Back to Movies</button>
                </div>

                {showsForMovie.length === 0 ? <p>No shows scheduled for this movie.</p> : (
                  <div className="shows-list-admin">
                    {showsForMovie.map(show => (
                      <div key={show.id} className="admin-show-item" style={{ padding: '15px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', marginBottom: '10px', border: '1px solid var(--border)' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                          <div>
                            <strong>{show.screen.theater.name}</strong> ({show.screen.name})
                            <p style={{ margin: '5px 0', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                              Start: {new Date(show.startTime).toLocaleString()}
                            </p>
                          </div>
                          <div style={{ display: 'flex', gap: '8px' }}>
                            <button className="btn-secondary" style={{ padding: '6px 12px', fontSize: '0.8rem' }} onClick={() => { setEditingShowId(show.id); setEditShowTime(show.startTime.substring(0, 16)); }}>Edit Time</button>
                            <button className="btn-primary" style={{ padding: '6px 12px', fontSize: '0.8rem', background: '#ff4d4d' }} onClick={() => handleDeleteShow(show.id)}>Delete</button>
                          </div>
                        </div>

                        {editingShowId === show.id && (
                          <div style={{ marginTop: '15px', padding: '15px', background: 'rgba(255,255,255,0.05)', borderRadius: '8px', border: '1px solid var(--accent)' }}>
                            <label className="input-label">New Start Time</label>
                            <div style={{ display: 'flex', gap: '10px' }}>
                              <input type="datetime-local" className="input-field" value={editShowTime} onChange={e => setEditShowTime(e.target.value)} />
                              <button className="btn-primary" onClick={() => handleUpdateTime(show.id)} disabled={loading}>Save</button>
                              <button className="btn-secondary" onClick={() => setEditingShowId(null)}>Cancel</button>
                            </div>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
