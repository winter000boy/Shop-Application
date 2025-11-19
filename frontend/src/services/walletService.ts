// Wallet service - API calls for wallet and transactions

import apiService from './api';
import { API_ENDPOINTS } from '../utils/constants';
import { PaginatedResponse, PaginationParams } from '../types';

export interface Wallet {
  id: number;
  shopId: number;
  balance: number;
  referralCode: string;
  updatedAt: string;
}

export interface Transaction {
  id: number;
  type: 'CREDIT' | 'DEBIT';
  amount: number;
  description: string;
  balanceAfter: number;
  createdAt: string;
}

export interface ReferralStats {
  referralCode: string;
  totalReferrals: number;
  totalEarnings: number;
  referrals: ReferralDetail[];
}

export interface ReferralDetail {
  id: number;
  referredShopName: string;
  bonusAmount: number;
  createdAt: string;
}

export interface ApplyReferralRequest {
  referralCode: string;
}

class WalletService {
  async getWallet(): Promise<Wallet> {
    return await apiService.get<Wallet>(API_ENDPOINTS.WALLET.BASE);
  }

  async getTransactions(params?: PaginationParams): Promise<PaginatedResponse<Transaction>> {
    return await apiService.get<PaginatedResponse<Transaction>>(
      API_ENDPOINTS.WALLET.TRANSACTIONS,
      { params }
    );
  }

  async getReferralStats(): Promise<ReferralStats> {
    return await apiService.get<ReferralStats>(API_ENDPOINTS.WALLET.REFERRAL);
  }

  async applyReferralCode(referralCode: string): Promise<void> {
    const request: ApplyReferralRequest = { referralCode };
    return await apiService.post<void>(API_ENDPOINTS.WALLET.APPLY_REFERRAL, request);
  }
}

export const walletService = new WalletService();
export default walletService;
