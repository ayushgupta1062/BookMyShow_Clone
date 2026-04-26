import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth();
  const [scrolled, setScrolled] = useState(false);
  const [dropOpen, setDropOpen] = useState(false);
  const [searchVal, setSearchVal] = useState('');
  const navigate = useNavigate();
  const dropRef = useRef(null);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', onScroll);
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  useEffect(() => {
    const handler = (e) => { if (dropRef.current && !dropRef.current.contains(e.target)) setDropOpen(false); };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchVal.trim()) { navigate(`/movies?search=${searchVal}`); setSearchVal(''); }
  };

  return (
    <nav className={`navbar ${scrolled ? 'scrolled' : ''}`}>
      <div className="nav-inner">
        <Link to="/" className="nav-logo">
          <span className="logo-icon-svg">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><rect width="18" height="18" x="3" y="3" rx="2" ry="2"></rect><path d="M7 3v18"></path><path d="M17 3v18"></path><path d="M3 7h4"></path><path d="M3 12h18"></path><path d="M3 17h4"></path><path d="M17 17h4"></path><path d="M17 12h4"></path><path d="M17 7h4"></path></svg>
          </span>
          <span className="logo-text">Book<span>My</span>Show</span>
        </Link>

        <form className="nav-search" onSubmit={handleSearch}>
          <input
            value={searchVal}
            onChange={e => setSearchVal(e.target.value)}
            placeholder="Search movies..."
            className="nav-search-input"
          />
          <button type="submit" className="nav-search-btn">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.3-4.3"></path></svg>
          </button>
        </form>

        <div className="nav-links">
          <Link to="/movies">Movies</Link>
          {isAdmin && <Link to="/admin" className="admin-link">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{width:'16px',height:'16px',marginRight:'4px'}}><path d="m13 2-2 10h3L11 22l2-10h-3l2-10z"></path></svg>
            Admin
          </Link>}
        </div>

        <div className="nav-actions">
          {user ? (
            <div className="user-menu" ref={dropRef}>
              <button className="user-avatar" onClick={() => setDropOpen(p => !p)}>
                <span className="avatar-char">{user.name?.[0]?.toUpperCase()}</span>
                <span className="user-name">{user.name?.split(' ')[0]}</span>
                <span className="chevron-svg">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><path d="m6 9 6 6 6-6"></path></svg>
                </span>
              </button>
              {dropOpen && (
                <div className="dropdown-menu">
                  <div className="drop-header">
                    <div className="drop-name">{user.name}</div>
                    <div className="drop-email">{user.email}</div>
                    {isAdmin && <span className="badge badge-gold" style={{marginTop:'4px'}}>Admin</span>}
                  </div>
                  <div className="drop-divider"/>
                  <Link to="/profile" className="drop-item" onClick={() => setDropOpen(false)}>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                    My Bookings
                  </Link>
                  <button className="drop-item drop-logout" onClick={() => { logout(); setDropOpen(false); navigate('/'); }}>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" x2="9" y1="12" y2="12"></line></svg>
                    Logout
                  </button>
                </div>
              )}
            </div>
          ) : (
            <>
              <Link to="/login" className="btn-outline" style={{padding:'9px 20px',fontSize:'0.88rem'}}>Login</Link>
              <Link to="/signup" className="btn-primary" style={{padding:'9px 20px',fontSize:'0.88rem'}}>Sign Up</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
