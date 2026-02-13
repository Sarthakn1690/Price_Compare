import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
  timeout: 30000,
});

export const searchProductByUrl = (url) =>
  api.post('/products/search', { url }).then((r) => r.data);

export const getProduct = (id) =>
  api.get(`/products/${id}`).then((r) => r.data);

export const getPrices = (id) =>
  api.get(`/products/${id}/prices`).then((r) => r.data);

export const getHistory = (id, days = 14, platform = null) => {
  const params = { days };
  if (platform) params.platform = platform;
  return api.get(`/products/${id}/history`, { params }).then((r) => r.data);
};

export const getRecommendation = (id) =>
  api.get(`/products/${id}/recommendation`).then((r) => r.data);

export const trackProduct = (id, userId = null) => {
  const params = userId ? { userId } : {};
  return api.post(`/products/${id}/track`, null, { params }).then((r) => r.data);
};

export default api;
