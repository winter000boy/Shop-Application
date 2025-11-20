// Customer list component with table and search

import { useState } from 'react';
import { CustomerResponse } from '../../types';
import Table, { Column } from '../common/Table';

interface CustomerListProps {
  customers: CustomerResponse[];
  loading?: boolean;
  onCustomerClick: (customer: CustomerResponse) => void;
  onSearch: (query: string, searchBy: string) => void;
}

const CustomerList = ({
  customers,
  loading = false,
  onCustomerClick,
  onSearch,
}: CustomerListProps) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchBy, setSearchBy] = useState('all');

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    onSearch(searchQuery, searchBy);
  };

  const columns: Column<CustomerResponse>[] = [
    {
      key: 'name',
      header: 'Name',
      render: (customer) => (
        <div className="font-medium text-gray-900">{customer.name}</div>
      ),
    },
    {
      key: 'phoneNumber',
      header: 'Phone Number',
      render: (customer) => (
        <div className="text-gray-600">{customer.phoneNumber}</div>
      ),
    },
    {
      key: 'email',
      header: 'Email',
      render: (customer) => (
        <div className="text-gray-600">{customer.email || '-'}</div>
      ),
    },
    {
      key: 'repairOrderCount',
      header: 'Orders',
      render: (customer) => (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
          {customer.repairOrderCount}
        </span>
      ),
    },
    {
      key: 'createdAt',
      header: 'Added On',
      render: (customer) => (
        <div className="text-gray-600">
          {new Date(customer.createdAt).toLocaleDateString()}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      {/* Search Bar */}
      <form onSubmit={handleSearch} className="flex gap-2">
        <select
          value={searchBy}
          onChange={(e) => setSearchBy(e.target.value)}
          className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="all">All Fields</option>
          <option value="name">Name</option>
          <option value="phone">Phone</option>
          <option value="email">Email</option>
        </select>
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Search customers..."
          className="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <button
          type="submit"
          className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          Search
        </button>
        {searchQuery && (
          <button
            type="button"
            onClick={() => {
              setSearchQuery('');
              onSearch('', 'all');
            }}
            className="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-500"
          >
            Clear
          </button>
        )}
      </form>

      {/* Customer Table */}
      <div className="bg-white shadow rounded-lg overflow-hidden">
        <Table
          data={customers}
          columns={columns}
          onRowClick={onCustomerClick}
          loading={loading}
          emptyMessage="No customers found. Add your first customer to get started."
        />
      </div>
    </div>
  );
};

export default CustomerList;
