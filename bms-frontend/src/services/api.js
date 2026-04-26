import axios from 'axios';

const API = axios.create({ baseURL: 'http://localhost:8080/api' });

API.interceptors.request.use(config => {
  const token = localStorage.getItem('bms_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export const authAPI = {
  signup: (data) => API.post('/auth/signup', data),
  login: (data) => API.post('/auth/login', data),
  me: () => API.get('/auth/me'),
};

export const movieAPI = {
  getAll: () => API.get('/movies'),
  getById: (id) => API.get(`/movies/${id}`),
  search: (title) => API.get(`/movies/search?title=${title}`),
  create: (data) => API.post('/movies', data),
  update: (id, data) => API.put(`/movies/${id}`, data),
  delete: (id) => API.delete(`/movies/${id}`),
};

export const showAPI = {
  getAll: () => API.get('/shows'),
  getById: (id) => API.get(`/shows/${id}`),
  getByMovie: (movieId) => API.get(`/shows/movie/${movieId}`),
  getByMovieAndCity: (movieId, city) => API.get(`/shows/movie/${movieId}/city/${city}`),
  recommendSeats: (showId, count) => API.get(`/shows/${showId}/recommend-seats?count=${count}`),
  create: (data) => API.post('/shows', data),
  delete: (id) => API.delete(`/shows/${id}`),
  updateTime: (id, startTime) => API.put(`/shows/${id}/time`, startTime, { headers: { 'Content-Type': 'application/json' } }),
};

export const bookingAPI = {
  create: (data) => API.post('/bookings', data),
  getById: (id) => API.get(`/bookings/${id}`),
  getByUser: (userId) => API.get(`/bookings/user/${userId}`),
  cancel: (id) => API.delete(`/bookings/${id}/cancel`),
};

export const cartAPI = {
  create: (data) => API.post('/cart', data),
  getById: (cartId) => API.get(`/cart/${cartId}`),
  getUserCarts: (userId) => API.get(`/cart/user/${userId}`),
  confirm: (cartId, paymentMethod) => API.post(`/cart/${cartId}/confirm?paymentMethod=${paymentMethod}`),
};

export const theaterAPI = {
  getAll: () => API.get('/theaters'),
  getById: (id) => API.get(`/theaters/${id}`),
  getByCity: (city) => API.get(`/theaters/city/${city}`),
  create: (data) => API.post('/theaters', data),
};

export const chatAPI = {
  start: (userId) => API.post(`/chat/start${userId ? `?userId=${userId}` : ''}`),
  sendMessage: (data) => API.post('/chat/message', data),
  getSession: (sessionId) => API.get(`/chat/session/${sessionId}`),
};

export const screenAPI = {
  getByTheater: (theaterId) => API.get(`/screens/theater/${theaterId}`),
};

export default API;
