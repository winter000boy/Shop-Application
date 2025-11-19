// Application constants

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

export const API_ENDPOINTS = {
  // Auth endpoints
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    REFRESH: '/auth/refresh',
    LOGOUT: '/auth/logout',
    ME: '/auth/me',
  },
  // Shop endpoints
  SHOP: {
    PROFILE: '/shops/profile',
    LOGO: '/shops/logo',
    SETTINGS: '/shops/settings',
  },
  // Order endpoints
  ORDERS: {
    BASE: '/orders',
    BY_ID: (id: number) => `/orders/${id}`,
    STATUS: (id: number) => `/orders/${id}/status`,
    IMAGES: (id: number) => `/orders/${id}/images`,
    NOTIFY: (id: number) => `/orders/${id}/notify`,
  },
  // Customer endpoints
  CUSTOMERS: {
    BASE: '/customers',
    BY_ID: (id: number) => `/customers/${id}`,
    ORDERS: (id: number) => `/customers/${id}/orders`,
  },
  // Product endpoints
  PRODUCTS: {
    BASE: '/products',
    BY_ID: (id: number) => `/products/${id}`,
    IMAGES: (id: number) => `/products/${id}/images`,
  },
  // Marketplace endpoints
  MARKETPLACE: {
    PRODUCTS: '/marketplace/products',
    CATEGORIES: '/marketplace/categories',
    WHOLESALERS: '/marketplace/wholesalers',
    SEARCH: '/marketplace/search',
  },
  // Wallet endpoints
  WALLET: {
    BASE: '/wallet',
    TRANSACTIONS: '/wallet/transactions',
    REFERRAL: '/wallet/referral',
    APPLY_REFERRAL: '/wallet/referral/apply',
  },
  // Staff endpoints
  STAFF: {
    BASE: '/staff',
    BY_ID: (id: number) => `/staff/${id}`,
    PERFORMANCE: (id: number) => `/staff/${id}/performance`,
  },
  // Community endpoints
  COMMUNITY: {
    POSTS: '/community/posts',
    POST_BY_ID: (id: number) => `/community/posts/${id}`,
    REPLIES: (id: number) => `/community/posts/${id}/replies`,
  },
  // Invoice endpoints
  INVOICES: {
    BASE: '/invoices',
    BY_ID: (id: number) => `/invoices/${id}`,
    PDF: (id: number) => `/invoices/${id}/pdf`,
    SEND: (id: number) => `/invoices/${id}/send`,
  },
};

export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'accessToken',
  REFRESH_TOKEN: 'refreshToken',
  USER: 'user',
};

export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  DASHBOARD: '/dashboard',
  ORDERS: '/orders',
  CUSTOMERS: '/customers',
  MARKETPLACE: '/marketplace',
  COMMUNITY: '/community',
  WALLET: '/wallet',
  STAFF: '/staff',
  INVOICES: '/invoices',
  SETTINGS: '/settings',
};

export const DEFAULT_PAGE_SIZE = 20;
