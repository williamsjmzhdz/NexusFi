import axios from 'axios';

const api = axios.create({
  baseURL: 'https://nexusfi-production.up.railway.app/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach JWT token automatically to every request if present
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
