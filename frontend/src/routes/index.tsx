// Route configuration

import { Routes, Route, Navigate } from 'react-router-dom';
import { ROUTES } from '../utils/constants';
import Login from '../pages/Login';
import Register from '../pages/Register';
import Dashboard from '../pages/Dashboard';
import Customers from '../pages/Customers';
import Orders from '../pages/Orders';
import Marketplace from '../pages/Marketplace';
import Wallet from '../pages/Wallet';
import ProtectedRoute from '../components/auth/ProtectedRoute';
import DashboardLayout from '../components/layout/DashboardLayout';

const AppRoutes = () => {
  return (
    <Routes>
      {/* Public routes */}
      <Route path={ROUTES.LOGIN} element={<Login />} />
      <Route path={ROUTES.REGISTER} element={<Register />} />
      
      {/* Protected routes with dashboard layout */}
      <Route
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route path={ROUTES.DASHBOARD} element={<Dashboard />} />
        {/* Placeholder routes for other sections - will be implemented in future tasks */}
        <Route path={ROUTES.ORDERS} element={<Orders />} />
        <Route path={ROUTES.CUSTOMERS} element={<Customers />} />
        <Route path={ROUTES.MARKETPLACE} element={<Marketplace />} />
        <Route path={ROUTES.COMMUNITY} element={<div className="p-6"><h1 className="text-2xl font-bold">Community</h1><p className="text-gray-600 mt-2">Coming soon...</p></div>} />
        <Route path={ROUTES.WALLET} element={<Wallet />} />
        <Route path={ROUTES.STAFF} element={<div className="p-6"><h1 className="text-2xl font-bold">Staff</h1><p className="text-gray-600 mt-2">Coming soon...</p></div>} />
        <Route path={ROUTES.INVOICES} element={<div className="p-6"><h1 className="text-2xl font-bold">Invoices</h1><p className="text-gray-600 mt-2">Coming soon...</p></div>} />
        <Route path={ROUTES.SETTINGS} element={<div className="p-6"><h1 className="text-2xl font-bold">Settings</h1><p className="text-gray-600 mt-2">Coming soon...</p></div>} />
      </Route>
      
      {/* Default redirect */}
      <Route path={ROUTES.HOME} element={<Navigate to={ROUTES.DASHBOARD} replace />} />
      
      {/* Catch all - redirect to dashboard */}
      <Route path="*" element={<Navigate to={ROUTES.DASHBOARD} replace />} />
    </Routes>
  );
};

export default AppRoutes;
