// Authentication related types

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  shopName: string;
  shopType: ShopType;
  gstNumber?: string;
  ownerName: string;
  username: string;
  phoneNumber: string;
  countryCode: string;
  address: string;
  email: string;
  password: string;
  logoFile?: File;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: UserRole;
  shopId: number;
  active: boolean;
}

export enum UserRole {
  ADMIN = 'ADMIN',
  STAFF = 'STAFF',
}

export enum ShopType {
  MOBILE_REPAIR = 'MOBILE_REPAIR',
  HARDWARE_REPAIR = 'HARDWARE_REPAIR',
  ELECTRONICS_REPAIR = 'ELECTRONICS_REPAIR',
}

export interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}
