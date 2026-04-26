import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { cartAPI } from '../services/api';
import './CartPage.css';

export default function CartPage() {
  const { cartId } = useParams();
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [processing, setProcessing] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState('CARD');

  useEffect(() => {
    cartAPI.getById(cartId)
      .then(res => setCart(res.data))
      .catch(err => setError(err.response?.data?.message || 'Cart not found'))
      .finally(() => setLoading(false));
  }, [cartId]);

  const handleConfirm = () => {
    setProcessing(true);
    cartAPI.confirm(cartId, paymentMethod)
      .then(res => {
        // Assume booking API returns booking number
        navigate(`/booking/${res.data.id}`);
      })
      .catch(err => {
        alert(err.response?.data?.message || 'Failed to confirm booking.');
        setProcessing(false);
      });
  };

  if (loading) return <div className="page container"><div className="skeleton" style={{height:'400px'}}></div></div>;
  if (error || !cart) return <div className="page container"><h2>{error || "Cart not found"}</h2></div>;

  if (cart.status !== 'ACTIVE') {
    return (
      <div className="page container empty-state">
        <div className="empty-icon">⏳</div>
        <h3>Cart {cart.status.toLowerCase()}</h3>
        <p>This cart is no longer active. Please start a new booking.</p>
        <button className="btn-primary mt-3" onClick={() => navigate('/')}>Go Home</button>
      </div>
    );
  }

  return (
    <div className="page container cart-page">
      <h2 className="section-title mb-4">Checkout <span className="gradient-text">Summary</span></h2>
      
      <div className="cart-layout">
        <div className="cart-details glass-card">
          <div className="cart-movie-info">
            <img src={cart.show.movie.posterUrl} alt={cart.show.movie.title} className="cart-poster" />
            <div>
              <h3>{cart.show.movie.title}</h3>
              <p className="text-secondary">{cart.show.screen.theater.name}</p>
              <p className="text-secondary">{new Date(cart.show.startTime).toLocaleString()}</p>
            </div>
          </div>
          
          <div className="divider" />
          
          <h4 className="mb-3">Selected Seats ({cart.seats.length})</h4>
          <div className="cart-seats-list">
            {cart.seats.map(seat => (
              <div key={seat.id} className="cart-seat-item">
                <span>Seat {seat.seat.seatNumber} ({seat.seat.seatType})</span>
                <span>₹{seat.price.toFixed(2)}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="cart-summary glass-card">
          <h3 className="mb-4">Payment Details</h3>
          
          <div className="summary-row">
            <span>Subtotal</span>
            <span>₹{cart.totalPrice.toFixed(2)}</span>
          </div>
          <div className="summary-row text-secondary">
            <span>Internet Handling Fee</span>
            <span>₹{(cart.totalPrice * 0.1).toFixed(2)}</span>
          </div>
          <div className="summary-row text-secondary">
            <span>Taxes (18%)</span>
            <span>₹{(cart.totalPrice * 0.18).toFixed(2)}</span>
          </div>
          
          <div className="divider" />
          
          <div className="summary-row total-row">
            <span>Total Payable</span>
            <span>₹{(cart.totalPrice * 1.28).toFixed(2)}</span>
          </div>

          <div className="payment-methods mt-4">
            <h4 className="mb-3">Select Payment Method</h4>
            <select 
              className="input-field" 
              value={paymentMethod} 
              onChange={e => setPaymentMethod(e.target.value)}
            >
              <option value="CARD">Credit/Debit Card</option>
              <option value="UPI">UPI</option>
              <option value="NETBANKING">Net Banking</option>
            </select>
          </div>

          <button 
            className="btn-primary w-100 mt-4" 
            onClick={handleConfirm}
            disabled={processing}
            style={{padding: '16px', fontSize: '1.1rem'}}
          >
            {processing ? 'Processing Payment...' : `Pay ₹${(cart.totalPrice * 1.28).toFixed(2)}`}
          </button>
          <p className="cart-timer text-muted text-center mt-3">
            <small>Cart expires at {new Date(cart.expiresAt).toLocaleTimeString()}</small>
          </p>
        </div>
      </div>
    </div>
  );
}
