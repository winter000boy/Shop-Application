// Sidebar navigation component

import { NavLink } from 'react-router-dom';
import { ROUTES } from '../../utils/constants';
import {
  HomeIcon,
  WrenchScrewdriverIcon,
  UsersIcon,
  ShoppingBagIcon,
  ChatBubbleLeftRightIcon,
  WalletIcon,
  UserGroupIcon,
  DocumentTextIcon,
  Cog6ToothIcon,
  XMarkIcon,
} from '@heroicons/react/24/outline';

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

const navigationItems = [
  { name: 'Dashboard', href: ROUTES.DASHBOARD, icon: HomeIcon },
  { name: 'Repair Orders', href: ROUTES.ORDERS, icon: WrenchScrewdriverIcon },
  { name: 'Customers', href: ROUTES.CUSTOMERS, icon: UsersIcon },
  { name: 'Marketplace', href: ROUTES.MARKETPLACE, icon: ShoppingBagIcon },
  { name: 'Community', href: ROUTES.COMMUNITY, icon: ChatBubbleLeftRightIcon },
  { name: 'Wallet', href: ROUTES.WALLET, icon: WalletIcon },
  { name: 'Staff', href: ROUTES.STAFF, icon: UserGroupIcon },
  { name: 'Invoices', href: ROUTES.INVOICES, icon: DocumentTextIcon },
  { name: 'Settings', href: ROUTES.SETTINGS, icon: Cog6ToothIcon },
];

const Sidebar = ({ isOpen, onClose }: SidebarProps) => {
  return (
    <>
      {/* Mobile sidebar */}
      <div
        className={`fixed inset-y-0 left-0 z-30 w-64 transform bg-white shadow-lg transition-transform duration-300 ease-in-out lg:hidden ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex h-full flex-col">
          {/* Logo and close button */}
          <div className="flex h-16 items-center justify-between border-b border-gray-200 px-4">
            <div className="flex items-center">
              <WrenchScrewdriverIcon className="h-8 w-8 text-blue-600" />
              <span className="ml-2 text-xl font-bold text-gray-900">ShopMS</span>
            </div>
            <button
              onClick={onClose}
              className="rounded-md p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
            >
              <XMarkIcon className="h-6 w-6" />
            </button>
          </div>

          {/* Navigation */}
          <nav className="flex-1 space-y-1 overflow-y-auto px-3 py-4">
            {navigationItems.map((item) => (
              <NavLink
                key={item.name}
                to={item.href}
                onClick={onClose}
                className={({ isActive }) =>
                  `flex items-center rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-blue-50 text-blue-600'
                      : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
                  }`
                }
              >
                <item.icon className="mr-3 h-6 w-6 flex-shrink-0" />
                {item.name}
              </NavLink>
            ))}
          </nav>
        </div>
      </div>

      {/* Desktop sidebar */}
      <div className="hidden lg:fixed lg:inset-y-0 lg:z-10 lg:flex lg:w-64 lg:flex-col">
        <div className="flex flex-col bg-white border-r border-gray-200">
          {/* Logo */}
          <div className="flex h-16 items-center border-b border-gray-200 px-6">
            <WrenchScrewdriverIcon className="h-8 w-8 text-blue-600" />
            <span className="ml-2 text-xl font-bold text-gray-900">ShopMS</span>
          </div>

          {/* Navigation */}
          <nav className="flex-1 space-y-1 overflow-y-auto px-3 py-4">
            {navigationItems.map((item) => (
              <NavLink
                key={item.name}
                to={item.href}
                className={({ isActive }) =>
                  `flex items-center rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-blue-50 text-blue-600'
                      : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
                  }`
                }
              >
                <item.icon className="mr-3 h-6 w-6 flex-shrink-0" />
                {item.name}
              </NavLink>
            ))}
          </nav>
        </div>
      </div>
    </>
  );
};

export default Sidebar;
