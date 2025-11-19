// Authentication service

import apiService from './api';
import { API_ENDPOINTS, STORAGE_KEYS } from '../utils/constants';
import { LoginRequest, RegisterRequest, AuthResponse, User, RefreshTokenRequest } from '../types';

class AuthService {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await apiService.post<AuthResponse>(
      API_ENDPOINTS.AUTH.LOGIN,
      credentials
    );

    // Store tokens and user in localStorage
    this.storeAuthData(response);

    return response;
  }

  async register(data: RegisterRequest): Promise<AuthResponse> {
    // If logo file is provided, use FormData
    if (data.logoFile) {
      const formData = new FormData();
      formData.append('shopName', data.shopName);
      formData.append('shopType', data.shopType);
      if (data.gstNumber) formData.append('gstNumber', data.gstNumber);
      formData.append('ownerName', data.ownerName);
      formData.append('username', data.username);
      formData.append('phoneNumber', data.phoneNumber);
      formData.append('countryCode', data.countryCode);
      formData.append('address', data.address);
      formData.append('email', data.email);
      formData.append('password', data.password);
      formData.append('logoFile', data.logoFile);

      const response = await apiService.uploadFile<AuthResponse>(
        API_ENDPOINTS.AUTH.REGISTER,
        formData
      );

      this.storeAuthData(response);
      return response;
    } else {
      const response = await apiService.post<AuthResponse>(
        API_ENDPOINTS.AUTH.REGISTER,
        data
      );

      this.storeAuthData(response);
      return response;
    }
  }

  async refreshToken(refreshToken: string): Promise<AuthResponse> {
    const request: RefreshTokenRequest = { refreshToken };
    const response = await apiService.post<AuthResponse>(
      API_ENDPOINTS.AUTH.REFRESH,
      request
    );

    this.storeAuthData(response);
    return response;
  }

  async logout(): Promise<void> {
    try {
      await apiService.post(API_ENDPOINTS.AUTH.LOGOUT);
    } catch (error) {
      // Continue with logout even if API call fails
      console.error('Logout error:', error);
    } finally {
      this.clearAuthData();
    }
  }

  async getCurrentUser(): Promise<User> {
    return await apiService.get<User>(API_ENDPOINTS.AUTH.ME);
  }

  private storeAuthData(authResponse: AuthResponse): void {
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, authResponse.accessToken);
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, authResponse.refreshToken);
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(authResponse.user));
  }

  private clearAuthData(): void {
    localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
  }

  getStoredUser(): User | null {
    const userStr = localStorage.getItem(STORAGE_KEYS.USER);
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch {
        return null;
      }
    }
    return null;
  }

  getStoredToken(): string | null {
    return localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
  }

  isAuthenticated(): boolean {
    return !!this.getStoredToken();
  }
}

export const authService = new AuthService();
export default authService;
