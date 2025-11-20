// Wallet page - Display wallet balance, transactions, and referral information

import { useState, useEffect } from 'react';
import WalletBalance from '../components/wallet/WalletBalance';
import TransactionHistory from '../components/wallet/TransactionHistory';
import ReferralLink from '../components/wallet/ReferralLink';
import walletService, { Wallet as WalletType, Transaction, ReferralStats } from '../services/walletService';
import { DEFAULT_PAGE_SIZE } from '../utils/constants';

const Wallet = () => {
  const [wallet, setWallet] = useState<WalletType | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [referralStats, setReferralStats] = useState<ReferralStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [transactionsLoading, setTransactionsLoading] = useState(true);
  const [referralLoading, setReferralLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchWallet();
    fetchReferralStats();
  }, []);

  useEffect(() => {
    fetchTransactions(currentPage);
  }, [currentPage]);

  const fetchWallet = async () => {
    try {
      setLoading(true);
      const data = await walletService.getWallet();
      setWallet(data);
      setError(null);
    } catch (err) {
      console.error('Failed to fetch wallet:', err);
      setError('Failed to load wallet information');
    } finally {
      setLoading(false);
    }
  };

  const fetchTransactions = async (page: number) => {
    try {
      setTransactionsLoading(true);
      const response = await walletService.getTransactions({
        page,
        size: DEFAULT_PAGE_SIZE,
        sort: 'createdAt,desc',
      });
      setTransactions(response.content);
      setTotalPages(response.totalPages);
      setError(null);
    } catch (err) {
      console.error('Failed to fetch transactions:', err);
      setError('Failed to load transactions');
    } finally {
      setTransactionsLoading(false);
    }
  };

  const fetchReferralStats = async () => {
    try {
      setReferralLoading(true);
      const data = await walletService.getReferralStats();
      setReferralStats(data);
      setError(null);
    } catch (err) {
      console.error('Failed to fetch referral stats:', err);
      setError('Failed to load referral information');
    } finally {
      setReferralLoading(false);
    }
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Wallet</h1>
        <p className="text-gray-600 mt-1">
          Manage your wallet balance, view transactions, and track referral earnings
        </p>
      </div>

      {error && (
        <div className="mb-6 rounded-md bg-red-50 p-4">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg
                className="h-5 w-5 text-red-400"
                fill="currentColor"
                viewBox="0 0 20 20"
              >
                <path
                  fillRule="evenodd"
                  d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                  clipRule="evenodd"
                />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm font-medium text-red-800">{error}</p>
            </div>
          </div>
        </div>
      )}

      <div className="space-y-6">
        {/* Wallet Balance */}
        <WalletBalance
          balance={wallet?.balance || 0}
          referralCode={wallet?.referralCode || ''}
          loading={loading}
        />

        {/* Transaction History */}
        <TransactionHistory
          transactions={transactions}
          loading={transactionsLoading}
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={handlePageChange}
        />

        {/* Referral Link and Stats */}
        <ReferralLink referralStats={referralStats} loading={referralLoading} />
      </div>
    </div>
  );
};

export default Wallet;
