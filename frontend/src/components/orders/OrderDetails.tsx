// Order details component with images and timeline

import { RepairOrder, OrderStatus } from '../../types';
import OrderStatusBadge from './OrderStatusBadge';

interface OrderDetailsProps {
  order: RepairOrder;
  onStatusUpdate: (status: OrderStatus) => void;
  onNotifyCustomer: () => void;
  onUploadImages: (files: File[]) => void;
  onClose: () => void;
  loading?: boolean;
}

const OrderDetails = ({
  order,
  onStatusUpdate,
  onNotifyCustomer,
  onUploadImages,
  onClose,
  loading = false,
}: OrderDetailsProps) => {
  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    if (files.length > 0) {
      onUploadImages(files);
    }
  };

  const getNextStatus = (): OrderStatus | null => {
    switch (order.status) {
      case OrderStatus.PENDING:
        return OrderStatus.IN_PROGRESS;
      case OrderStatus.IN_PROGRESS:
        return OrderStatus.COMPLETED;
      case OrderStatus.COMPLETED:
        return OrderStatus.DELIVERED;
      default:
        return null;
    }
  };

  const nextStatus = getNextStatus();

  return (
    <div className="bg-white rounded-lg shadow-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto">
      {/* Header */}
      <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Order #{order.id}</h2>
          <p className="text-sm text-gray-500 mt-1">
            Created on {new Date(order.createdAt).toLocaleString()}
          </p>
        </div>
        <button
          onClick={onClose}
          className="text-gray-400 hover:text-gray-600 focus:outline-none"
        >
          <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <div className="px-6 py-4 space-y-6">
        {/* Status and Actions */}
        <div className="flex items-center justify-between">
          <div>
            <label className="text-sm font-medium text-gray-700 block mb-2">Current Status</label>
            <OrderStatusBadge status={order.status} />
          </div>
          <div className="flex gap-2">
            {nextStatus && (
              <button
                onClick={() => onStatusUpdate(nextStatus)}
                disabled={loading}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
              >
                {loading ? 'Updating...' : `Move to ${nextStatus.replace('_', ' ')}`}
              </button>
            )}
            <button
              onClick={onNotifyCustomer}
              disabled={loading}
              className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 disabled:opacity-50"
            >
              Notify Customer
            </button>
          </div>
        </div>

        {/* Customer Information */}
        <div className="border-t border-gray-200 pt-4">
          <h3 className="text-lg font-semibold text-gray-900 mb-3">Customer Information</h3>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-gray-500">Name</label>
              <p className="text-gray-900">{order.customer.name}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Phone</label>
              <p className="text-gray-900">{order.customer.phoneNumber}</p>
            </div>
            {order.customer.email && (
              <div>
                <label className="text-sm font-medium text-gray-500">Email</label>
                <p className="text-gray-900">{order.customer.email}</p>
              </div>
            )}
            {order.customer.address && (
              <div className="col-span-2">
                <label className="text-sm font-medium text-gray-500">Address</label>
                <p className="text-gray-900">{order.customer.address}</p>
              </div>
            )}
          </div>
        </div>

        {/* Device Information */}
        <div className="border-t border-gray-200 pt-4">
          <h3 className="text-lg font-semibold text-gray-900 mb-3">Device Information</h3>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-gray-500">Device Model</label>
              <p className="text-gray-900">{order.deviceModel}</p>
            </div>
            {order.serialNumber && (
              <div>
                <label className="text-sm font-medium text-gray-500">Serial Number</label>
                <p className="text-gray-900">{order.serialNumber}</p>
              </div>
            )}
            {order.lockCode && (
              <div>
                <label className="text-sm font-medium text-gray-500">Lock Code</label>
                <p className="text-gray-900 font-mono">{order.lockCode}</p>
              </div>
            )}
            <div className="col-span-2">
              <label className="text-sm font-medium text-gray-500">Problem Description</label>
              <p className="text-gray-900">{order.problemDescription}</p>
            </div>
            {order.accessories && (
              <div className="col-span-2">
                <label className="text-sm font-medium text-gray-500">Accessories</label>
                <p className="text-gray-900">{order.accessories}</p>
              </div>
            )}
          </div>
        </div>

        {/* Pricing Information */}
        <div className="border-t border-gray-200 pt-4">
          <h3 className="text-lg font-semibold text-gray-900 mb-3">Pricing</h3>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-gray-500">Estimated Price</label>
              <p className="text-gray-900 text-lg font-semibold">₹{order.estimatedPrice.toFixed(2)}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Paid Amount</label>
              <p className="text-gray-900 text-lg font-semibold">₹{order.paidAmount.toFixed(2)}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Balance Due</label>
              <p className="text-gray-900 text-lg font-semibold text-red-600">
                ₹{(order.estimatedPrice - order.paidAmount).toFixed(2)}
              </p>
            </div>
            {order.cashbackEnabled && order.cashbackAmount && (
              <div>
                <label className="text-sm font-medium text-gray-500">Cashback</label>
                <p className="text-green-600 text-lg font-semibold">₹{order.cashbackAmount.toFixed(2)}</p>
              </div>
            )}
          </div>
        </div>

        {/* Additional Information */}
        <div className="border-t border-gray-200 pt-4">
          <h3 className="text-lg font-semibold text-gray-900 mb-3">Additional Information</h3>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-gray-500">Repair Date</label>
              <p className="text-gray-900">{new Date(order.repairDate).toLocaleDateString()}</p>
            </div>
            {order.assignedStaff && (
              <div>
                <label className="text-sm font-medium text-gray-500">Assigned Staff</label>
                <p className="text-gray-900">{order.assignedStaff.name}</p>
              </div>
            )}
            {order.warrantyDays && (
              <div>
                <label className="text-sm font-medium text-gray-500">Warranty</label>
                <p className="text-gray-900">{order.warrantyDays} days</p>
              </div>
            )}
            {order.expenses && (
              <div className="col-span-2">
                <label className="text-sm font-medium text-gray-500">Expenses</label>
                <p className="text-gray-900">{order.expenses}</p>
              </div>
            )}
          </div>
        </div>

        {/* Images */}
        <div className="border-t border-gray-200 pt-4">
          <h3 className="text-lg font-semibold text-gray-900 mb-3">Images</h3>
          <div className="space-y-4">
            {order.images && order.images.length > 0 ? (
              <div className="grid grid-cols-3 gap-4">
                {order.images.map((image) => (
                  <div key={image.id} className="relative aspect-square">
                    <img
                      src={image.imageUrl}
                      alt="Order"
                      className="w-full h-full object-cover rounded-lg border border-gray-200"
                    />
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500 text-sm">No images uploaded yet</p>
            )}
            <div>
              <label
                htmlFor="image-upload"
                className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer"
              >
                <svg className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
                Upload Images
              </label>
              <input
                id="image-upload"
                type="file"
                multiple
                accept="image/*"
                onChange={handleImageUpload}
                className="hidden"
              />
            </div>
          </div>
        </div>

        {/* Timeline */}
        <div className="border-t border-gray-200 pt-4">
          <h3 className="text-lg font-semibold text-gray-900 mb-3">Timeline</h3>
          <div className="space-y-3">
            <div className="flex items-start">
              <div className="flex-shrink-0">
                <div className="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center">
                  <svg className="h-5 w-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                  </svg>
                </div>
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-900">Order Created</p>
                <p className="text-sm text-gray-500">{new Date(order.createdAt).toLocaleString()}</p>
              </div>
            </div>
            {order.updatedAt !== order.createdAt && (
              <div className="flex items-start">
                <div className="flex-shrink-0">
                  <div className="h-8 w-8 rounded-full bg-green-100 flex items-center justify-center">
                    <svg className="h-5 w-5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11a1 1 0 10-2 0v3.586L7.707 9.293a1 1 0 00-1.414 1.414l3 3a1 1 0 001.414 0l3-3a1 1 0 00-1.414-1.414L11 10.586V7z" clipRule="evenodd" />
                    </svg>
                  </div>
                </div>
                <div className="ml-3">
                  <p className="text-sm font-medium text-gray-900">Last Updated</p>
                  <p className="text-sm text-gray-500">{new Date(order.updatedAt).toLocaleString()}</p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default OrderDetails;
