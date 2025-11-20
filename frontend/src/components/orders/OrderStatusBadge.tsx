// Order status badge component for visual status display

import { OrderStatus } from '../../types';

interface OrderStatusBadgeProps {
  status: OrderStatus;
}

const OrderStatusBadge = ({ status }: OrderStatusBadgeProps) => {
  const getStatusStyles = () => {
    switch (status) {
      case OrderStatus.PENDING:
        return 'bg-yellow-100 text-yellow-800';
      case OrderStatus.IN_PROGRESS:
        return 'bg-blue-100 text-blue-800';
      case OrderStatus.COMPLETED:
        return 'bg-green-100 text-green-800';
      case OrderStatus.DELIVERED:
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusLabel = () => {
    switch (status) {
      case OrderStatus.PENDING:
        return 'Pending';
      case OrderStatus.IN_PROGRESS:
        return 'In Progress';
      case OrderStatus.COMPLETED:
        return 'Completed';
      case OrderStatus.DELIVERED:
        return 'Delivered';
      default:
        return status;
    }
  };

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusStyles()}`}
    >
      {getStatusLabel()}
    </span>
  );
};

export default OrderStatusBadge;
