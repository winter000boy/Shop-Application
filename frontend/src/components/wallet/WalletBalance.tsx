// WalletBalance component - Display current wallet balance

import { formatCurrency } from '../../utils/formatters';

interface WalletBalanceProps {
  balance: number;
  referralCode: string;
  loading?: boolean;
}

const WalletBalance = ({ balance, referralCode, loading }: WalletBalanceProps) => {
  if (loading) {
    return (
      <div className="overflow-hidden rounded-lg bg-white shadow">
        <div className="p-6">
          <div className="animate-pulse">
            <div className="h-4 bg-gray-200 rounded w-1/4 mb-4"></div>
            <div className="h-10 bg-gray-200 rounded w-1/2"></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="overflow-hidden rounded-lg bg-gradient-to-br from-blue-500 to-blue-600 shadow-lg">
      <div className="p-6">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-blue-100">Wallet Balance</p>
            <p className="mt-2 text-4xl font-bold text-white">
              {formatCurrency(balance)}
            </p>
          </div>
          <div className="rounded-full bg-white bg-opacity-20 p-3">
            <svg
              className="h-8 w-8 text-white"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"
              />
            </svg>
          </div>
        </div>
        <div className="mt-4 pt-4 border-t border-blue-400">
          <p className="text-xs font-medium text-blue-100">Your Referral Code</p>
          <p className="mt-1 text-lg font-semibold text-white tracking-wider">
            {referralCode}
          </p>
        </div>
      </div>
    </div>
  );
};

export default WalletBalance;
