import { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('bms_token');
    const savedUser = localStorage.getItem('bms_user');
    if (token && savedUser) {
      const parsedUser = JSON.parse(savedUser);
      // Migration safety: if user has old 'userId' but no 'id', move it.
      if (parsedUser.userId && !parsedUser.id) {
        parsedUser.id = parsedUser.userId;
      }
      setUser(parsedUser);
    }
    setLoading(false);
  }, []);

  const login = async (credentials) => {
    const res = await authAPI.login(credentials);
    const { token, ...userData } = res.data;
    localStorage.setItem('bms_token', token);
    localStorage.setItem('bms_user', JSON.stringify(userData));
    setUser(userData);
    return userData;
  };

  const signup = async (data) => {
    const res = await authAPI.signup(data);
    const { token, ...userData } = res.data;
    localStorage.setItem('bms_token', token);
    localStorage.setItem('bms_user', JSON.stringify(userData));
    setUser(userData);
    return userData;
  };

  const logout = () => {
    localStorage.removeItem('bms_token');
    localStorage.removeItem('bms_user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, signup, logout, loading, isAdmin: user?.role === 'ROLE_ADMIN' }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
