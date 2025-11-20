// Orders page - Repair order management

import { useState, useEffect } from 'react';
import { RepairOrder, OrderRequest, OrderStatus, CustomerResponse, Staff } from '../types';
import orderService from '../services/orderService';
import customerService from '../services/customerService';
import staffService from '../services/staffService';
import OrderList from '../components/orders/OrderList';
import OrderForm from '../components/orders/OrderForm';
import OrderDetails from '../components/orders/OrderDetails';

type ViewMode = 'list' | 'create' | 'edit' | 'details';

const Orders = () => {
  const [orders, setOrders] = useState<RepairOrder[]>([]);
  const [customers, setCustomers] = useState<CustomerResponse[]>([]);
  const [staff, setStaff] = useState<Staff[]>([]);
  const [selectedOrder, setSelectedOrder] = useState<RepairOrder | null>(null);
  const [viewMode, setViewMode] = useState<ViewMode>('list');
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    fetchOrders();
    fetchCustomers();
    fetchStaff();
  }, []);

  useEffect(() => {
    if (statusFilter !== 'all') {
      fetchOrders(statusFilter);
    } else {
      fetchOrders();
    }
  }, [statusFilter]);

  const fetchOrders = async (status?: string) => {
    try {
      setLoading(true);
      const params: any = {};
      if (status && status !== 'all') {
        params.status = status;
      }
      const response = await orderService.getOrders(params);
      setOrders(response.content || []);
      setError(null);
    } catch (err) {
      setError('Failed to fetch orders');
      console.error('Error fetching orders:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchCustomers = async () => {
    try {
      const response = await customerService.getCustomers({ page: 0, size: 1000 });
      setCustomers(response.content || []);
    } catch (err) {
      console.error('Error fetching customers:', err);
    }
  };

  const fetchStaff = async () => {
    try {
      const response = await staffService.getStaff({ page: 0, size: 1000 });
      setStaff(response.content || []);
    } catch (err) {
      console.error('Error fetching staff:', err);
    }
  };

  const handleCreateOrder = async (data: OrderRequest) => {
    try {
      setLoading(true);
      await orderService.createOrder(data);
      setSuccessMessage('Order created successfully');
      setViewMode('list');
      fetchOrders();
    } catch (err) {
      setError('Failed to create order');
      console.error('Error creating order:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateOrder = async (data: OrderRequest) => {
    if (!selectedOrder) return;

    try {
      setLoading(true);
      await orderService.updateOrder(selectedOrder.id, data);
      setSuccessMessage('Order updated successfully');
      setViewMode('list');
      setSelectedOrder(null);
      fetchOrders();
    } catch (err) {
      setError('Failed to update order');
      console.error('Error updating order:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdate = async (status: OrderStatus) => {
    if (!selectedOrder) return;

    try {
      setLoading(true);
      const updatedOrder = await orderService.updateOrderStatus(selectedOrder.id, { status });
      setSelectedOrder(updatedOrder);
      setSuccessMessage(`Order status updated to ${status}`);
      fetchOrders();
    } catch (err) {
      setError('Failed to update order status');
      console.error('Error updating order status:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleNotifyCustomer = async () => {
    if (!selectedOrder) return;

    try {
      setLoading(true);
      await orderService.notifyCustomer(selectedOrder.id);
      setSuccessMessage('Customer notified successfully');
    } catch (err) {
      setError('Failed to notify customer');
      console.error('Error notifying customer:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleUploadImages = async (files: File[]) => {
    if (!selectedOrder) return;

    try {
      setLoading(true);
      const updatedOrder = await orderService.uploadOrderImages(selectedOrder.id, files);
      setSelectedOrder(updatedOrder);
      setSuccessMessage('Images uploaded successfully');
      fetchOrders();
    } catch (err) {
      setError('Failed to upload images');
      console.error('Error uploading images:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleOrderClick = (order: RepairOrder) => {
    setSelectedOrder(order);
    setViewMode('details');
  };

  const handleCancel = () => {
    setViewMode('list');
    setSelectedOrder(null);
  };

  const handleFilterChange = (status: string) => {
    setStatusFilter(status);
  };

  // Clear messages after 5 seconds
  useEffect(() => {
    if (successMessage) {
      const timer = setTimeout(() => setSuccessMessage(null), 5000);
      return () => clearTimeout(timer);
    }
  }, [successMessage]);

  useEffect(() => {
    if (error) {
      const timer = setTimeout(() => setError(null), 5000);
      return () => clearTimeout(timer);
    }
  }, [error]);

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Repair Orders</h1>
            <p className="text-gray-600 mt-1">Manage device repair orders and track progress</p>
          </div>
          {viewMode === 'list' && (
            <button
              onClick={() => setViewMode('create')}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              + New Order
            </button>
          )}
        </div>
      </div>

      {/* Success Message */}
      {successMessage && (
        <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-md">
          <p className="text-green-800">{successMessage}</p>
        </div>
      )}

      {/* Error Message */}
      {error && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-md">
          <p className="text-red-800">{error}</p>
        </div>
      )}

      {/* Content */}
      {viewMode === 'list' && (
        <OrderList
          orders={orders}
          loading={loading}
          onOrderClick={handleOrderClick}
          onFilterChange={handleFilterChange}
        />
      )}

      {viewMode === 'create' && (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Create New Order</h2>
          <OrderForm
            customers={customers}
            staff={staff}
            onSubmit={handleCreateOrder}
            onCancel={handleCancel}
            loading={loading}
          />
        </div>
      )}

      {viewMode === 'edit' && selectedOrder && (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Edit Order</h2>
          <OrderForm
            order={selectedOrder}
            customers={customers}
            staff={staff}
            onSubmit={handleUpdateOrder}
            onCancel={handleCancel}
            loading={loading}
          />
        </div>
      )}

      {viewMode === 'details' && selectedOrder && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <OrderDetails
            order={selectedOrder}
            onStatusUpdate={handleStatusUpdate}
            onNotifyCustomer={handleNotifyCustomer}
            onUploadImages={handleUploadImages}
            onClose={handleCancel}
            loading={loading}
          />
        </div>
      )}
    </div>
  );
};

export default Orders;
