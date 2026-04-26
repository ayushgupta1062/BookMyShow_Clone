import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Auth.css';

export default function Auth({ isLogin }) {
  const [formData, setFormData] = useState({ name: '', email: '', password: '', phoneNumber: '', adminCode: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, signup } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isLogin) {
        await login({ email: formData.email, password: formData.password });
      } else {
        await signup(formData);
      }
      
      const returnUrl = location.state?.returnTo || '/';
      navigate(returnUrl);
    } catch (err) {
      setError(err.response?.data?.message || 'Authentication failed');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = e => setFormData({...formData, [e.target.name]: e.target.value});

  return (
    <div className="auth-page">
      <div className="auth-card glass-card">
        <div className="auth-header text-center mb-4">
          <h2>{isLogin ? 'Welcome Back' : 'Create Account'}</h2>
          <p className="text-secondary">{isLogin ? 'Log in to continue booking' : 'Join the BookMyShow community'}</p>
        </div>
        
        {error && <div className="auth-error">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          {!isLogin && (
            <div className="form-group">
              <label className="input-label">Full Name</label>
              <input type="text" name="name" className="input-field" required value={formData.name} onChange={handleChange} />
            </div>
          )}

          <div className="form-group">
            <label className="input-label">Email Address</label>
            <input type="email" name="email" className="input-field" required value={formData.email} onChange={handleChange} />
          </div>

          {!isLogin && (
            <div className="form-group">
              <label className="input-label">Phone Number</label>
              <input type="tel" name="phoneNumber" className="input-field" required value={formData.phoneNumber} onChange={handleChange} />
            </div>
          )}

          <div className="form-group">
            <label className="input-label">Password</label>
            <input type="password" name="password" className="input-field" required value={formData.password} onChange={handleChange} />
          </div>

          {!isLogin && (
            <div className="form-group mt-4">
              <label className="input-label">Admin Code (Optional)</label>
              <input type="text" name="adminCode" className="input-field" placeholder="Secret code for admin access" value={formData.adminCode} onChange={handleChange} />
            </div>
          )}

          <button type="submit" className="btn-primary w-100 mt-4" disabled={loading}>
            {loading ? 'Processing...' : (isLogin ? 'Login' : 'Sign Up')}
          </button>
        </form>

        <div className="auth-footer text-center mt-4">
          {isLogin ? (
            <p>Don't have an account? <span className="auth-link" onClick={() => navigate('/signup')}>Sign up here</span></p>
          ) : (
            <p>Already have an account? <span className="auth-link" onClick={() => navigate('/login')}>Log in</span></p>
          )}
        </div>
      </div>
    </div>
  );
}
