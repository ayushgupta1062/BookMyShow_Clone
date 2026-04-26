import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { showAPI, cartAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import './ShowPage.css';

export default function ShowPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [show, setShow] = useState(null);
  const [seats, setSeats] = useState([]);
  const [selectedSeatIds, setSelectedSeatIds] = useState([]);
  const [recommendedSeatIds, setRecommendedSeatIds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    fetchShowData();
  }, [id]);

  const fetchShowData = () => {
    showAPI.getById(id)
      .then(res => {
        setShow(res.data);
        setSeats(res.data.availableSeats || []);
        // Get recommendations automatically if we have available seats
        if (res.data.availableSeats && res.data.availableSeats.length > 0) {
          getRecommendations(2); // default suggest 2
        } else {
          setLoading(false);
        }
      })
      .catch(err => {
        console.error(err);
        setError('Failed to load show details.');
        setLoading(false);
      });
  };

  const getRecommendations = (count) => {
    showAPI.recommendSeats(id, count)
      .then(res => {
        const ids = res.data.recommendedSeats.map(s => s.id);
        setRecommendedSeatIds(ids);
      })
      .catch(err => console.error("Rec error:", err))
      .finally(() => setLoading(false));
  };

  const toggleSeat = (seatId) => {
    setSelectedSeatIds(prev => {
      if (prev.includes(seatId)) return prev.filter(id => id !== seatId);
      if (prev.length >= 10) { alert("You can select up to 10 seats max."); return prev; }
      return [...prev, seatId];
    });
  };

  const selectRecommended = () => {
    setSelectedSeatIds(recommendedSeatIds);
  };

  const proceedToBook = () => {
    if (selectedSeatIds.length === 0) return alert("Please select at least one seat.");
    if (!user) return navigate('/login', { state: { returnTo: `/shows/${id}` } });

    setActionLoading(true);
    cartAPI.create({
      userId: user.id,
      showId: show.id,
      seatIds: selectedSeatIds
    })
    .then(res => {
      navigate(`/cart/${res.data.cartId}`);
    })
    .catch(err => {
      console.error(err);
      const msg = err.response?.data?.message || "They might have been taken.";
      alert(`Failed to reserve seats: ${msg}`);
      fetchShowData(); // refresh
    })
    .finally(() => setActionLoading(false));
  };

  if (loading) return <div className="page container"><div className="skeleton" style={{height:'80vh', borderRadius:'16px'}}></div></div>;
  if (error || !show) return <div className="page container"><h2>{error || "Show not found"}</h2></div>;

  // Group seats by Row (Letter)
  const rows = {};
  seats.forEach(seat => {
    const seatNum = seat.seat.seatNumber;
    const rowLetter = seatNum.charAt(0).toUpperCase(); // assuming format like "A1", "B5"
    if (!rows[rowLetter]) rows[rowLetter] = [];
    rows[rowLetter].push(seat);
  });
  
  // Sort rows alphabetically, then by seat number numerically
  const sortedRowKeys = Object.keys(rows).sort();
  sortedRowKeys.forEach(r => {
    rows[r].sort((a,b) => {
      const numA = parseInt(a.seat.seatNumber.replace(/\D/g, '')) || 0;
      const numB = parseInt(b.seat.seatNumber.replace(/\D/g, '')) || 0;
      return numA - numB;
    });
  });

  const totalAmount = selectedSeatIds.reduce((sum, sId) => {
    const s = seats.find(x => x.id === sId);
    return sum + (s ? s.price : 0);
  }, 0);

  return (
    <div className="page show-page container">
      <div className="show-header glass-card">
        <div>
          <h2 className="show-movie">{show.movie.title}</h2>
          <p className="show-meta">
            {show.screen.theater.name} | {show.screen.name} | {new Date(show.startTime).toLocaleString()}
          </p>
        </div>
        {recommendedSeatIds.length > 0 && (
          <div className="recommendation-banner">
            <span className="sparkles">✨</span>
            <div>
              <strong>AI Recommendation</strong>
              <p>We found {recommendedSeatIds.length} perfect seats for you!</p>
            </div>
            <button className="btn-gold" onClick={selectRecommended} disabled={actionLoading}>
              Select Recommended
            </button>
          </div>
        )}
      </div>

      <div className="seating-area">
        <div className="screen-container">
          <div className="screen-arc"></div>
          <p className="screen-text">SCREEN THIS WAY</p>
        </div>

        <div className="seat-grid-container">
          {sortedRowKeys.map(rowKey => (
            <div key={rowKey} className="seat-row">
              <div className="row-label">{rowKey}</div>
              <div className="seats">
                {rows[rowKey].map(seatObj => {
                  const isAvailable = seatObj.status === 'AVAILABLE';
                  const isSelected = selectedSeatIds.includes(seatObj.id);
                  const isRecommended = recommendedSeatIds.includes(seatObj.id) && !isSelected;

                  let className = "seat-btn ";
                  if (!isAvailable) className += "seat-booked";
                  else if (isSelected) className += "seat-selected";
                  else if (isRecommended) className += "seat-recommended";
                  else className += "seat-available";

                  return (
                    <button
                      key={seatObj.id}
                      className={className}
                      disabled={!isAvailable || actionLoading}
                      onClick={() => toggleSeat(seatObj.id)}
                      title={`₹${seatObj.price} - ${seatObj.seat.seatType}`}
                    >
                      {seatObj.seat.seatNumber.replace(/\D/g, '')}
                    </button>
                  );
                })}
              </div>
            </div>
          ))}
        </div>

        <div className="seat-legend">
          <div className="legend-item"><span className="seat-box seat-available"></span> Available</div>
          <div className="legend-item"><span className="seat-box seat-selected"></span> Selected</div>
          <div className="legend-item"><span className="seat-box seat-recommended"></span> AI Recommended</div>
          <div className="legend-item"><span className="seat-box seat-booked"></span> Sold</div>
        </div>
      </div>

      {selectedSeatIds.length > 0 && (
        <div className="booking-bar glass-card">
          <div className="booking-summary">
            <span className="seat-count">{selectedSeatIds.length} Tickets selected</span>
            <h3 className="total-price">Total: ₹{totalAmount.toFixed(2)}</h3>
          </div>
          <button className="btn-primary" onClick={proceedToBook} disabled={actionLoading}>
            {actionLoading ? 'Reserving...' : 'Proceed to Pay'}
          </button>
        </div>
      )}
    </div>
  );
}
