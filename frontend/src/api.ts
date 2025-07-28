import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080',
});

export interface AuthResponse {
  accessToken: string;
  refreshTooken: string; // note: server uses this misspelling
}

export interface RegisterPayload {
  username: string;
  password: string;
  fullName: string;
  email: string;
}

export async function login(username: string, password: string) {
  const res = await api.post<AuthResponse>('/auth/login', { username, password });
  return res.data;
}

export async function register(payload: RegisterPayload) {
  const res = await api.post('/auth/register', payload);
  return res.data as { userId: string };
}

export async function verifyOtp(userId: string, code: string) {
  const res = await api.post<AuthResponse>(`/auth/${userId}/verify`, { verificationCode: code }, { params: { userId } });
  return res.data;
}

export default api;

