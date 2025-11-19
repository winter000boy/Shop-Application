# Frontend Project Structure

This document describes the frontend project structure for the Shop Management System.

## Directory Structure

```
frontend/src/
├── components/          # Reusable React components
│   ├── common/         # Common UI components (Button, Input, Modal, etc.)
│   └── layout/         # Layout components (Sidebar, Header, etc.)
├── pages/              # Page components for routes
│   ├── Dashboard.tsx   # Main dashboard page
│   ├── Login.tsx       # Login page
│   └── Register.tsx    # Registration page
├── routes/             # React Router configuration
│   └── index.tsx       # Route definitions
├── services/           # API service layer
│   ├── api.ts          # Base Axios instance with interceptors
│   ├── authService.ts  # Authentication API calls
│   ├── orderService.ts # Order management API calls
│   ├── productService.ts # Product API calls
│   └── walletService.ts # Wallet API calls
├── store/              # Redux Toolkit store
│   ├── store.ts        # Store configuration
│   └── authSlice.ts    # Authentication state slice
├── hooks/              # Custom React hooks
│   ├── useAuth.ts      # Auth hook for accessing auth state
│   └── useAppDispatch.ts # Typed Redux hooks
├── types/              # TypeScript type definitions
│   ├── index.ts        # Type exports
│   ├── common.types.ts # Common types (ApiResponse, Pagination)
│   ├── auth.types.ts   # Authentication types
│   ├── order.types.ts  # Order types
│   └── product.types.ts # Product types
├── utils/              # Utility functions
│   ├── constants.ts    # App constants (API endpoints, routes)
│   ├── validators.ts   # Validation functions
│   └── formatters.ts   # Formatting functions (currency, date)
├── App.tsx             # Root App component
├── main.tsx            # Application entry point
└── index.css           # Global styles
```

## Key Features Implemented

### 1. Redux Toolkit Store
- Configured Redux store with auth slice
- Async thunks for login, register, logout
- Persistent authentication state using localStorage
- Type-safe state management

### 2. React Router
- Route configuration with React Router v6
- Public routes (Login, Register)
- Protected routes (Dashboard) - to be enhanced in task 15
- Default redirects and catch-all routes

### 3. API Service Layer
- Axios instance with base configuration
- Request interceptor for JWT token injection
- Response interceptor for automatic token refresh
- Error handling with standardized error responses
- Support for file uploads and downloads
- Automatic redirect to login on authentication failure

### 4. Authentication Service
- Login and registration with JWT tokens
- Token storage in localStorage
- Token refresh mechanism
- User session management
- Logout functionality

### 5. Type Definitions
- Comprehensive TypeScript types for all entities
- API response types with generics
- Pagination types
- Authentication types (User, LoginRequest, RegisterRequest)
- Order, Product, and other domain types

### 6. Utility Functions
- Constants for API endpoints and routes
- Validation functions (email, phone, password, GST)
- Formatting functions (currency, date, phone)
- Storage key constants

### 7. Custom Hooks
- `useAuth`: Access auth state and actions
- `useAppDispatch` and `useAppSelector`: Typed Redux hooks

## API Integration

### Base API Service (`services/api.ts`)
The API service provides:
- Automatic JWT token injection in request headers
- Token refresh on 401 responses
- Standardized error handling
- Type-safe HTTP methods (get, post, put, delete, patch)
- File upload support with multipart/form-data
- File download functionality

### Authentication Flow
1. User submits login credentials
2. API service sends request to `/api/auth/login`
3. Server responds with access token, refresh token, and user data
4. Tokens stored in localStorage
5. Redux store updated with user data
6. Subsequent requests include JWT token in Authorization header
7. On token expiry (401), interceptor automatically refreshes token
8. If refresh fails, user is redirected to login

### Token Refresh Mechanism
- Implemented in API service response interceptor
- Automatically triggered on 401 Unauthorized responses
- Uses refresh token to obtain new access token
- Retries original request with new token
- Clears auth data and redirects to login if refresh fails

## Environment Variables

Create a `.env` file in the frontend directory:

```
VITE_API_BASE_URL=http://localhost:8080/api
VITE_FIREBASE_API_KEY=your_firebase_api_key
VITE_FIREBASE_STORAGE_BUCKET=your_firebase_storage_bucket
```

## Next Steps

The following features will be implemented in subsequent tasks:

- **Task 15**: Authentication UI components (LoginForm, RegisterForm, ProtectedRoute)
- **Task 16**: Dashboard layout with sidebar and header
- **Task 17-24**: Feature-specific UI components and pages
- **Task 26**: Common UI components (Button, Input, Modal, etc.)
- **Task 27**: Error handling and loading states

## Usage Examples

### Using the Auth Hook
```typescript
import { useAuth } from '../hooks/useAuth';

function LoginPage() {
  const { login, isLoading, error } = useAuth();

  const handleSubmit = async (credentials) => {
    try {
      await login(credentials);
      // Redirect to dashboard
    } catch (err) {
      // Handle error
    }
  };
}
```

### Making API Calls
```typescript
import orderService from '../services/orderService';

async function fetchOrders() {
  try {
    const orders = await orderService.getOrders({ page: 0, size: 20 });
    console.log(orders);
  } catch (error) {
    console.error('Failed to fetch orders:', error);
  }
}
```

### Using Redux Store
```typescript
import { useAppSelector, useAppDispatch } from '../hooks/useAppDispatch';
import { login } from '../store/authSlice';

function Component() {
  const dispatch = useAppDispatch();
  const user = useAppSelector(state => state.auth.user);
  
  const handleLogin = () => {
    dispatch(login({ email: 'test@example.com', password: 'password' }));
  };
}
```

## Requirements Satisfied

This implementation satisfies the following requirements:

- **Requirement 3.1**: Dashboard with organized sections for all management features
- **Requirement 3.2**: Responsive navigation that adapts to different screen sizes
- **Requirement 3.3**: Navigation to corresponding feature modules
