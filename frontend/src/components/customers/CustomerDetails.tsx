// Customer details component showing repair history

import { CustomerResponse, RepairOrderSummary, OrderStatus } from '../../types';

interface CustomerDetailsProps {
  customer: CustomerResponse;
  orders: RepairOrderSummary[];
  loading?: boolean;
  onClose: () => void;
  onEdit: () => void;
}

const CustomerDetails = ({
  customer,
  orders,
  loading = false,
  onClose,
  onEdit,
}: CustomerDetailsProps) => {
  const getStatusColor = (status: OrderStatus) => {
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

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
    }).format(amount);
  };

  return (
    <div className="bg-white rounded-lg shadow-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto">
      {/* Header */}
      <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center sticky top-0 bg-white z-10">
        <h2 className="text-xl font-semibold text-gray-900">Customer Details</h2>
        <button
          onClick={onClose}
          className="text-gray-400 hover:text-gray-600 focus:outline-none"
        >
          <svg
            className="h-6 w-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>

      {/* Customer Information */}
      <div className="px-6 py-4 border-b border-gray-200">
        <div className="flex justify-between items-start mb-4">
          <div>
            <h3 className="text-lg font-medium text-gray-900">{customer.name}</h3>
            <p className="text-sm text-gray-500">
              Customer since {new Date(customer.createdAt).toLocaleDateString()}
            </p>
          </div>
          <button
            onClick={onEdit}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            Edit
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <p className="text-sm font-medium text-gray-500">Phone Number</p>
            <p className="mt-1 text-sm text-gray-900">{customer.phoneNumber}</p>
          </div>
          <div>
            <p className="text-sm font-medium text-gray-500">Email</p>
            <p className="mt-1 text-sm text-gray-900">
              {customer.email || 'Not provided'}
            </p>
          </div>
          <div className="md:col-span-2">
            <p className="text-sm font-medium text-gray-500">Address</p>
            <p className="mt-1 text-sm text-gray-900">
              {customer.address || 'Not provided'}
            </p>
          </div>
          <div>
            <p className="text-sm font-medium text-gray-500">Total Orders</p>
            <p className="mt-1 text-sm text-gray-900">
              {customer.repairOrderCount}
            </p>
          </div>
        </div>
      </div>

      {/* Repair History */}
      <div className="px-6 py-4">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Repair History</h3>

        {loading ? (
          <div className="flex justify-center items-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          </div>
        ) : orders.length === 0 ? (
          <p className="text-center text-gray-500 py-8">
            No repair orders found for this customer.
          </p>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => (
              <div
                key={order.id}
                className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
              >
                <div className="flex justify-between items-start mb-2">
                  <div>
                    <h4 className="font-medium text-gray-900">
                      {order.deviceModel}
                    </h4>
                    <p className="text-sm text-gray-500">
                      Order #{order.id}
                    </p>
                  </div>
                  <span
                    className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(
                      order.status
                    )}`}
                  >
                    {order.status.replace('_', ' ')}
                  </span>
                </div>

                <p className="text-sm text-gray-600 mb-3">
                  {order.problemDescription}
                </p>

                <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-sm">
                  <div>
                    <p className="text-gray-500">Estimated Price</p>
                    <p className="font-medium text-gray-900">
                      {formatCurrency(order.estimatedPrice)}
                    </p>
                  </div>
                  <div>
                    <p className="text-gray-500">Paid Amount</p>
                    <p className="font-medium text-gray-900">
                      {formatCurrency(order.paidAmount)}
                    </p>
                  </div>
                  <div>
                    <p className="text-gray-500">Repair Date</p>
                    <p className="font-medium text-gray-900">
                      {new Date(order.repairDate).toLocaleDateString()}
                    </p>
                  </div>
                  <div>
                    <p className="text-gray-500">Assigned To</p>
                    <p className="font-medium text-gray-900">
                      {order.assignedStaffName || 'Unassigned'}
                    </p>
                  </div>
                </div>

                {order.warrantyDays && (
                  <div className="mt-3 pt-3 border-t border-gray-200">
                    <p className="text-sm text-gray-600">
                      Warranty: {order.warrantyDays} days
                    </p>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default CustomerDetails;
