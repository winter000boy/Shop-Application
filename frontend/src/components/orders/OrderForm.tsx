// Order form component with all order fields

import { useState, useEffect } from 'react';
import { RepairOrder, OrderRequest, CustomerResponse, Staff } from '../../types';

interface OrderFormProps {
  order?: RepairOrder;
  customers: CustomerResponse[];
  staff: Staff[];
  onSubmit: (data: OrderRequest) => void;
  onCancel: () => void;
  loading?: boolean;
}

const OrderForm = ({
  order,
  customers,
  staff,
  onSubmit,
  onCancel,
  loading = false,
}: OrderFormProps) => {
  const [formData, setFormData] = useState<OrderRequest>({
    customerId: 0,
    deviceModel: '',
    problemDescription: '',
    estimatedPrice: 0,
    paidAmount: 0,
    lockCode: '',
    repairDate: new Date().toISOString().split('T')[0],
    accessories: '',
    serialNumber: '',
    assignedStaffId: undefined,
    cashbackEnabled: false,
    cashbackAmount: 0,
    warrantyDays: 0,
    expenses: '',
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (order) {
      setFormData({
        customerId: order.customer.id,
        deviceModel: order.deviceModel,
        problemDescription: order.problemDescription,
        estimatedPrice: order.estimatedPrice,
        paidAmount: order.paidAmount,
        lockCode: order.lockCode || '',
        repairDate: order.repairDate.split('T')[0],
        accessories: order.accessories || '',
        serialNumber: order.serialNumber || '',
        assignedStaffId: order.assignedStaff?.id,
        cashbackEnabled: order.cashbackEnabled,
        cashbackAmount: order.cashbackAmount || 0,
        warrantyDays: order.warrantyDays || 0,
        expenses: order.expenses || '',
      });
    }
  }, [order]);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.customerId || formData.customerId === 0) {
      newErrors.customerId = 'Customer is required';
    }

    if (!formData.deviceModel.trim()) {
      newErrors.deviceModel = 'Device model is required';
    }

    if (!formData.problemDescription.trim()) {
      newErrors.problemDescription = 'Problem description is required';
    }

    if (formData.estimatedPrice <= 0) {
      newErrors.estimatedPrice = 'Estimated price must be greater than 0';
    }

    if (formData.paidAmount < 0) {
      newErrors.paidAmount = 'Paid amount cannot be negative';
    }

    if (formData.paidAmount > formData.estimatedPrice) {
      newErrors.paidAmount = 'Paid amount cannot exceed estimated price';
    }

    if (!formData.repairDate) {
      newErrors.repairDate = 'Repair date is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validateForm()) {
      onSubmit(formData);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;

    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : type === 'number' ? parseFloat(value) || 0 : value,
    }));

    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Customer */}
        <div>
          <label htmlFor="customerId" className="block text-sm font-medium text-gray-700">
            Customer <span className="text-red-500">*</span>
          </label>
          <select
            id="customerId"
            name="customerId"
            value={formData.customerId}
            onChange={handleChange}
            className={`mt-1 block w-full px-3 py-2 border ${
              errors.customerId ? 'border-red-500' : 'border-gray-300'
            } rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500`}
          >
            <option value={0}>Select a customer</option>
            {customers.map((customer) => (
              <option key={customer.id} value={customer.id}>
                {customer.name} - {customer.phoneNumber}
              </option>
            ))}
          </select>
          {errors.customerId && (
            <p className="mt-1 text-sm text-red-600">{errors.customerId}</p>
          )}
        </div>

        {/* Device Model */}
        <div>
          <label htmlFor="deviceModel" className="block text-sm font-medium text-gray-700">
            Device Model <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            id="deviceModel"
            name="deviceModel"
            value={formData.deviceModel}
            onChange={handleChange}
            className={`mt-1 block w-full px-3 py-2 border ${
              errors.deviceModel ? 'border-red-500' : 'border-gray-300'
            } rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500`}
            placeholder="e.g., iPhone 13 Pro"
          />
          {errors.deviceModel && (
            <p className="mt-1 text-sm text-red-600">{errors.deviceModel}</p>
          )}
        </div>

        {/* Estimated Price */}
        <div>
          <label htmlFor="estimatedPrice" className="block text-sm font-medium text-gray-700">
            Estimated Price <span className="text-red-500">*</span>
          </label>
          <input
            type="number"
            id="estimatedPrice"
            name="estimatedPrice"
            value={formData.estimatedPrice}
            onChange={handleChange}
            step="0.01"
            min="0"
            className={`mt-1 block w-full px-3 py-2 border ${
              errors.estimatedPrice ? 'border-red-500' : 'border-gray-300'
            } rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500`}
            placeholder="0.00"
          />
          {errors.estimatedPrice && (
            <p className="mt-1 text-sm text-red-600">{errors.estimatedPrice}</p>
          )}
        </div>

        {/* Paid Amount */}
        <div>
          <label htmlFor="paidAmount" className="block text-sm font-medium text-gray-700">
            Paid Amount <span className="text-red-500">*</span>
          </label>
          <input
            type="number"
            id="paidAmount"
            name="paidAmount"
            value={formData.paidAmount}
            onChange={handleChange}
            step="0.01"
            min="0"
            className={`mt-1 block w-full px-3 py-2 border ${
              errors.paidAmount ? 'border-red-500' : 'border-gray-300'
            } rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500`}
            placeholder="0.00"
          />
          {errors.paidAmount && (
            <p className="mt-1 text-sm text-red-600">{errors.paidAmount}</p>
          )}
        </div>

        {/* Repair Date */}
        <div>
          <label htmlFor="repairDate" className="block text-sm font-medium text-gray-700">
            Repair Date <span className="text-red-500">*</span>
          </label>
          <input
            type="date"
            id="repairDate"
            name="repairDate"
            value={formData.repairDate}
            onChange={handleChange}
            className={`mt-1 block w-full px-3 py-2 border ${
              errors.repairDate ? 'border-red-500' : 'border-gray-300'
            } rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500`}
          />
          {errors.repairDate && (
            <p className="mt-1 text-sm text-red-600">{errors.repairDate}</p>
          )}
        </div>

        {/* Assigned Staff */}
        <div>
          <label htmlFor="assignedStaffId" className="block text-sm font-medium text-gray-700">
            Assigned Staff
          </label>
          <select
            id="assignedStaffId"
            name="assignedStaffId"
            value={formData.assignedStaffId || ''}
            onChange={handleChange}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="">Unassigned</option>
            {staff.map((member) => (
              <option key={member.id} value={member.id}>
                {member.name}
              </option>
            ))}
          </select>
        </div>

        {/* Lock Code */}
        <div>
          <label htmlFor="lockCode" className="block text-sm font-medium text-gray-700">
            Lock Code
          </label>
          <input
            type="text"
            id="lockCode"
            name="lockCode"
            value={formData.lockCode}
            onChange={handleChange}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            placeholder="Device lock code"
          />
        </div>

        {/* Serial Number */}
        <div>
          <label htmlFor="serialNumber" className="block text-sm font-medium text-gray-700">
            Serial Number
          </label>
          <input
            type="text"
            id="serialNumber"
            name="serialNumber"
            value={formData.serialNumber}
            onChange={handleChange}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            placeholder="Device serial number"
          />
        </div>

        {/* Warranty Days */}
        <div>
          <label htmlFor="warrantyDays" className="block text-sm font-medium text-gray-700">
            Warranty (Days)
          </label>
          <input
            type="number"
            id="warrantyDays"
            name="warrantyDays"
            value={formData.warrantyDays}
            onChange={handleChange}
            min="0"
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            placeholder="0"
          />
        </div>

        {/* Cashback Amount */}
        <div>
          <label htmlFor="cashbackAmount" className="block text-sm font-medium text-gray-700">
            Cashback Amount
          </label>
          <input
            type="number"
            id="cashbackAmount"
            name="cashbackAmount"
            value={formData.cashbackAmount}
            onChange={handleChange}
            step="0.01"
            min="0"
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            placeholder="0.00"
          />
        </div>
      </div>

      {/* Problem Description */}
      <div>
        <label htmlFor="problemDescription" className="block text-sm font-medium text-gray-700">
          Problem Description <span className="text-red-500">*</span>
        </label>
        <textarea
          id="problemDescription"
          name="problemDescription"
          value={formData.problemDescription}
          onChange={handleChange}
          rows={3}
          className={`mt-1 block w-full px-3 py-2 border ${
            errors.problemDescription ? 'border-red-500' : 'border-gray-300'
          } rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500`}
          placeholder="Describe the problem with the device"
        />
        {errors.problemDescription && (
          <p className="mt-1 text-sm text-red-600">{errors.problemDescription}</p>
        )}
      </div>

      {/* Accessories */}
      <div>
        <label htmlFor="accessories" className="block text-sm font-medium text-gray-700">
          Accessories
        </label>
        <textarea
          id="accessories"
          name="accessories"
          value={formData.accessories}
          onChange={handleChange}
          rows={2}
          className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
          placeholder="List any accessories received with the device"
        />
      </div>

      {/* Expenses */}
      <div>
        <label htmlFor="expenses" className="block text-sm font-medium text-gray-700">
          Expenses
        </label>
        <textarea
          id="expenses"
          name="expenses"
          value={formData.expenses}
          onChange={handleChange}
          rows={2}
          className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
          placeholder="Track expenses for this repair"
        />
      </div>

      {/* Cashback Enabled */}
      <div className="flex items-center">
        <input
          type="checkbox"
          id="cashbackEnabled"
          name="cashbackEnabled"
          checked={formData.cashbackEnabled}
          onChange={handleChange}
          className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
        />
        <label htmlFor="cashbackEnabled" className="ml-2 block text-sm text-gray-700">
          Enable cashback for this order
        </label>
      </div>

      {/* Form Actions */}
      <div className="flex justify-end gap-3">
        <button
          type="button"
          onClick={onCancel}
          disabled={loading}
          className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
        >
          Cancel
        </button>
        <button
          type="submit"
          disabled={loading}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
        >
          {loading ? 'Saving...' : order ? 'Update Order' : 'Create Order'}
        </button>
      </div>
    </form>
  );
};

export default OrderForm;
