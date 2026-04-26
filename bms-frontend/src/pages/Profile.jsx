import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { bookingAPI } from '../services/api';
import './Profile.css';

export default function Profile() {
  const { user } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (user && user.id) {
      bookingAPI.getByUser(user.id)
        .then(res => {
          setBookings(res.data);
          setLoading(false);
        })
        .catch(err => {
          console.error(err);
          setError('Failed to load your bookings.');
          setLoading(false);
        });
    }
  }, [user]);

  if (loading) return <div className="page container"><div className="skeleton" style={{height:'300px'}}></div></div>;

  return (
    <div className="page profile-page container">
      <div className="profile-header glass-card">
        <div className="profile-user-info">
          <div className="profile-user-avatar">{user?.name?.charAt(0)}</div>
          <div>
            <h1 className="profile-user-name">{user?.name}</h1>
            <p className="profile-user-email">{user?.email}</p>
          </div>
        </div>
      </div>

      <div className="bookings-section">
        <h2 className="section-title">My Bookings</h2>
        {error && <p className="error-text">{error}</p>}
        
        {bookings.length === 0 ? (
          <div className="no-bookings glass-card">
            <div className="empty-icon">🎟️</div>
            <h3>No bookings found</h3>
            <p>You haven't booked any movies yet. Start exploring!</p>
          </div>
        ) : (
          <div className="bookings-list">
            {bookings.map(booking => (
              <div key={booking.id} className="booking-card glass-card">
                <div className="booking-movie-poster">
                  <img src={booking.show.movie.posterUrl} alt={booking.show.movie.title} />
                </div>
                <div className="booking-details">
                  <div className="booking-main">
                    <div>
                      <h3 className="movie-title">{booking.show.movie.title}</h3>
                      <p className="theater-info">
                        {booking.show.screen.theater.name} | {booking.show.screen.name}
                      </p>
                      <p className="booking-time">
                        {new Date(booking.show.startTime).toLocaleDateString('en-IN', {
                          weekday: 'short', day: 'numeric', month: 'short', year: 'numeric'
                        })} • {new Date(booking.show.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </p>
                    </div>
                    <div className="booking-status-badge" data-status={booking.status}>
                      {booking.status}
                    </div>
                  </div>
                  
                  <div className="booking-meta">
                    <div className="meta-item">
                      <span className="label">Seats</span>
                      <span className="value">{booking.seats.map(s => s.seat.seatNumber).join(', ')}</span>
                    </div>
                    <div className="meta-item">
                      <span className="label">Booking ID</span>
                      <span className="value">#{booking.bookingNumber.substring(0, 8).toUpperCase()}</span>
                    </div>
                    <div className="meta-item">
                      <span className="label">Total Amount</span>
                      <span className="value amount">₹{booking.totalAmount.toFixed(2)}</span>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
