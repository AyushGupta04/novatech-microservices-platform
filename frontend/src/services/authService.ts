import { api } from './api';
import { ApiResponse, AuthResponse, User } from '../types';

export const authService = {
  async login(email: string, password: string):Promise<AuthResponse> {
    const response = await api.post<ApiResponse<AuthResponse>>('/api/v1/auth/login', {
      email,
      password,
    });
    return response.data.data;
  },

  async register(data: {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    phoneNumber?: string;
    roles?: string[];
  }): Promise<AuthResponse> {
    const response = await api.post<ApiResponse<AuthResponse>>('/api/v1/auth/register', data);
    return response.data.data;
  },

  async getCurrentUser(): Promise<User> {
    const response = await api.get<ApiResponse<User>>('/api/v1/auth/me');
    return response.data.data;
  },

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  },
};
