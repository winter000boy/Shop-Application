// Customers page integrating customer components

import { useState, useEffect } from 'react';
import { CustomerResponse, CustomerRequest, RepairOrderSummary } from '../types';
import CustomerList from '../components/customers/CustomerList';
import CustomerForm from '../components/customers/CustomerForm';
import CustomerDetails from '../components/customers/CustomerDetails';
import { customerService } from '../services/customerService';

type ModalMode = 'add' | 'edit' | 'view' | null;

const Customers = () => {
  const [customers, setCustomers] = useState<CustomerResponse[]>([]);
  const [selectedCustomer, setSelectedCustomer] = useState<CustomerResponse | null>(null);
  const [customerOrders, setCustomerOrders] = useState<RepairOrderSummary[]>([]);
  const [modalMode, setModalMode] = useState<ModalMode>(null);
  const [loading, setLoading] = useState(false);
  const [ordersLoading, setOrdersLoading] = useState(false);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
  });
  const [searchParams, setSearchParams] = useState({
    search: '',
    searchBy: 'all',
  });

  // Fetch customers
  const fetchCustomers = async (page = 0, search = '', searchBy = 'all') => {
    try {
      setLoading(true);
      const response = await customerService.getCustomers({
        page,
        size: 20,
        sortBy: 'createdAt',
        sortDir: 'DESC',
        search: search || undefined,
        searchBy: searchBy !== 'all' ? searchBy : undefined,
      });

      setCustomers(response.content);
      setPagination({
        page: response.page,
        size: response.size,
        totalElements: response.totalElements,
        totalPages: response.totalPages,
      });
    } catch (error) {
      console.error('Error fetching customers:', error);
      alert('Failed to load customers. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // Fetch customer orders
  const fetchCustomerOrders = async (customerId: number) => {
    try {
      setOrdersLoading(true);
      const orders = await customerService.getCustomerOrders(customerId);
      setCustomerOrders(orders);
    } catch (error) {
      console.error('Error fetching customer orders:', error);
      alert('Failed to load customer orders. Please try again.');
    } finally {
      setOrdersLoading(false);
    }
  };

  useEffect(() => {
    fetchCustomers();
  }, []);

  // Handle customer click - show details
  const handleCustomerClick = (customer: CustomerResponse) => {
    setSelectedCustomer(customer);
    setModalMode('view');
    fetchCustomerOrders(customer.id);
  };

  // Handle search
  const handleSearch = (query: string, searchBy: string) => {
    setSearchParams({ search: query, searchBy });
    fetchCustomers(0, query, searchBy);
  };

  // Handle add customer
  const handleAddCustomer = async (data: CustomerRequest) => {
    try {
      setLoading(true);
      await customerService.createCustomer(data);
      setModalMode(null);
      fetchCustomers(pagination.page, searchParams.search, searchParams.searchBy);
      alert('Customer added successfully!');
    } catch (error) {
      console.error('Error creating customer:', error);
      alert('Failed to add customer. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // Handle update customer
  const handleUpdateCustomer = async (data: CustomerRequest) => {
    if (!selectedCustomer) return;

    try {
      setLoading(true);
      await customerService.updateCustomer(selectedCustomer.id, data);
      setModalMode('view');
      fetchCustomers(pagination.page, searchParams.search, searchParams.searchBy);
      // Refresh selected customer
      const updatedCustomer = await customerService.getCustomerById(
        selectedCustomer.id
      );
      setSelectedCustomer(updatedCustomer);
      alert('Customer updated successfully!');
    } catch (error) {
      console.error('Error updating customer:', error);
      alert('Failed to update customer. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // Handle delete customer - commented out for future implementation
  // const handleDeleteCustomer = async (customerId: number) => {
  //   if (!confirm('Are you sure you want to delete this customer?')) return;

  //   try {
  //     setLoading(true);
  //     await customerService.deleteCustomer(customerId);
  //     setModalMode(null);
  //     setSelectedCustomer(null);
  //     fetchCustomers(pagination.page, searchParams.search, searchParams.searchBy);
  //     alert('Customer deleted successfully!');
  //   } catch (error) {
  //     console.error('Error deleting customer:', error);
  //     alert('Failed to delete customer. Please try again.');
  //   } finally {
  //     setLoading(false);
  //   }
  // };

  // Handle pagination
  const handlePageChange = (newPage: number) => {
    fetchCustomers(newPage, searchParams.search, searchParams.searchBy);
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Customers</h1>
          <p className="mt-1 text-sm text-gray-500">
            Manage your customer information and repair history
          </p>
        </div>
        <button
          onClick={() => {
            setSelectedCustomer(null);
            setModalMode('add');
          }}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          Add Customer
        </button>
      </div>

      {/* Customer List */}
      <CustomerList
        customers={customers}
        loading={loading}
        onCustomerClick={handleCustomerClick}
        onSearch={handleSearch}
      />

      {/* Pagination */}
      {pagination.totalPages > 1 && (
        <div className="flex justify-between items-center">
          <p className="text-sm text-gray-700">
            Showing {pagination.page * pagination.size + 1} to{' '}
            {Math.min(
              (pagination.page + 1) * pagination.size,
              pagination.totalElements
            )}{' '}
            of {pagination.totalElements} customers
          </p>
          <div className="flex gap-2">
            <button
              onClick={() => handlePageChange(pagination.page - 1)}
              disabled={pagination.page === 0}
              className="px-3 py-1 border border-gray-300 rounded-md text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
            >
              Previous
            </button>
            <span className="px-3 py-1 text-sm text-gray-700">
              Page {pagination.page + 1} of {pagination.totalPages}
            </span>
            <button
              onClick={() => handlePageChange(pagination.page + 1)}
              disabled={pagination.page >= pagination.totalPages - 1}
              className="px-3 py-1 border border-gray-300 rounded-md text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
            >
              Next
            </button>
          </div>
        </div>
      )}

      {/* Modal for Add/Edit Customer */}
      {(modalMode === 'add' || modalMode === 'edit') && (
        <div className="fixed inset-0 z-50 overflow-y-auto">
          <div className="flex items-center justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
            <div
              className="fixed inset-0 transition-opacity bg-gray-500 bg-opacity-75"
              onClick={() => setModalMode(null)}
            />
            <div className="inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full sm:p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                {modalMode === 'add' ? 'Add New Customer' : 'Edit Customer'}
              </h3>
              <CustomerForm
                customer={modalMode === 'edit' ? selectedCustomer || undefined : undefined}
                onSubmit={
                  modalMode === 'add' ? handleAddCustomer : handleUpdateCustomer
                }
                onCancel={() => setModalMode(null)}
                loading={loading}
              />
            </div>
          </div>
        </div>
      )}

      {/* Modal for Customer Details */}
      {modalMode === 'view' && selectedCustomer && (
        <div className="fixed inset-0 z-50 overflow-y-auto">
          <div className="flex items-center justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
            <div
              className="fixed inset-0 transition-opacity bg-gray-500 bg-opacity-75"
              onClick={() => setModalMode(null)}
            />
            <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-4xl sm:w-full">
              <CustomerDetails
                customer={selectedCustomer}
                orders={customerOrders}
                loading={ordersLoading}
                onClose={() => setModalMode(null)}
                onEdit={() => setModalMode('edit')}
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Customers;
