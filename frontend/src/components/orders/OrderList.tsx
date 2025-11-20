// Order list component with filtering by status

import { useState } from 'react';
import { RepairOrder, OrderStatus } from '../../types';
import Table, { Column } from '../common/Table';
import OrderStatusBadge from './OrderStatusBadge';

interface OrderListProps {
  orders: RepairOrder[];
  loading?: boolean;
  onOrderClick: (order: RepairOrder) => void;
  onFilterChange: (status: string) => void;
}

const OrderList = ({
  orders,
  loading = false,
  onOrderClick,
  onFilterChange,
}: OrderListProps) => {
  const [statusFilter, setStatusFilter] = useState<string>('all');

  const handleFilterChange = (status: string) => {
    setStatusFilter(status);
    onFilterChange(status);
  };

  const columns: Column<RepairOrder>[] = [
    {
      key: 'id',
      header: 'Order ID',
      render: (order) => (
        <div className="font-medium text-gray-900">#{order.id}</div>
      ),
    },
    {
      key: 'customer',
      header: 'Customer',
      render: (order) => (
        <div>
          <div className="font-medium text-gray-900">{order.customer.name}</div>
          <div className="text-sm text-gray-500">{order.customer.phoneNumber}</div>
        </div>
      ),
    },
    {
      key: 'deviceModel',
      header: 'Device',
      render: (order) => (
        <div className="text-gray-900">{order.deviceModel}</div>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      render: (order) => <OrderStatusBadge status={order.status} />,
    },
    {
      key: 'estimatedPrice',
      header: 'Estimated Price',
      render: (order) => (
        <div className="text-gray-900">₹{order.estimatedPrice.toFixed(2)}</div>
      ),
    },
    {
      key: 'paidAmount',
      header: 'Paid',
      render: (order) => (
        <div className="text-gray-900">₹{order.paidAmount.toFixed(2)}</div>
      ),
    },
    {
      key: 'assignedStaff',
      header: 'Assigned To',
      render: (order) => (
        <div className="text-gray-600">
          {order.assignedStaff?.name || 'Unassigned'}
        </div>
      ),
    },
    {
      key: 'repairDate',
      header: 'Repair Date',
      render: (order) => (
        <div className="text-gray-600">
          {new Date(order.repairDate).toLocaleDateString()}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      {/* Filter Bar */}
      <div className="flex gap-2 items-center">
        <label className="text-sm font-medium text-gray-700">Filter by Status:</label>
        <select
          value={statusFilter}
          onChange={(e) => handleFilterChange(e.target.value)}
          className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="all">All Orders</option>
          <option value={OrderStatus.PENDING}>Pending</option>
          <option value={OrderStatus.IN_PROGRESS}>In Progress</option>
          <option value={OrderStatus.COMPLETED}>Completed</option>
          <option value={OrderStatus.DELIVERED}>Delivered</option>
        </select>
      </div>

      {/* Order Table */}
      <div className="bg-white shadow rounded-lg overflow-hidden">
        <Table
          data={orders}
          columns={columns}
          onRowClick={onOrderClick}
          loading={loading}
          emptyMessage="No orders found. Create your first repair order to get started."
        />
      </div>
    </div>
  );
};

export default OrderList;
