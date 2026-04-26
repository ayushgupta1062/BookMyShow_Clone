import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import ChatbotWidget from './components/ChatbotWidget';

// Protected Route Wrapper
function AdminRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return null;
  return user && user.role === 'ROLE_ADMIN' ? children : <Navigate to="/" />;
}

// Pages
import HomePage from './pages/HomePage';
import MoviesPage from './pages/MoviesPage';
import MovieDetailPage from './pages/MovieDetailPage';
import ShowPage from './pages/ShowPage';
import CartPage from './pages/CartPage';
import Auth from './pages/Auth';
import AdminDashboard from './pages/AdminDashboard';
import Profile from './pages/Profile';

export default function App() {
  return (
    <AuthProvider>
      <Router>
        <Navbar />
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/movies" element={<MoviesPage />} />
          <Route path="/movies/:id" element={<MovieDetailPage />} />
          <Route path="/shows/:id" element={<ShowPage />} />
          <Route path="/cart/:cartId" element={<CartPage />} />
          
          {/* Auth */}
          <Route path="/login" element={<Auth isLogin={true} />} />
          <Route path="/signup" element={<Auth isLogin={false} />} />

          <Route path="/profile" element={<Profile />} />
          
          <Route path="/admin" element={<AdminRoute><AdminDashboard /></AdminRoute>} />
          
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
        <ChatbotWidget />
      </Router>
    </AuthProvider>
  );
}
