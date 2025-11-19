// Custom hook for accessing auth state and actions

import { useDispatch, useSelector } from 'react-redux';
import { useCallback } from 'react';
import { RootState, AppDispatch } from '../store/store';
import { login, register, logout, getCurrentUser, clearError } from '../store/authSlice';
import { LoginRequest, RegisterRequest } from '../types';

export const useAuth = () => {
  const dispatch = useDispatch<AppDispatch>();
  const auth = useSelector((state: RootState) => state.auth);

  const handleLogin = useCallback(
    async (credentials: LoginRequest) => {
      return await dispatch(login(credentials)).unwrap();
    },
    [dispatch]
  );

  const handleRegister = useCallback(
    async (data: RegisterRequest) => {
      return await dispatch(register(data)).unwrap();
    },
    [dispatch]
  );

  const handleLogout = useCallback(async () => {
    return await dispatch(logout()).unwrap();
  }, [dispatch]);

  const fetchCurrentUser = useCallback(async () => {
    return await dispatch(getCurrentUser()).unwrap();
  }, [dispatch]);

  const clearAuthError = useCallback(() => {
    dispatch(clearError());
  }, [dispatch]);

  return {
    user: auth.user,
    isAuthenticated: auth.isAuthenticated,
    isLoading: auth.isLoading,
    error: auth.error,
    login: handleLogin,
    register: handleRegister,
    logout: handleLogout,
    getCurrentUser: fetchCurrentUser,
    clearError: clearAuthError,
  };
};
