// User types
export interface User {
  id: string;
  email: string;
  displayName?: string;
  phoneNumber?: string;
  avatarUrl?: string;
  role: 'ADMIN' | 'OWNER' | 'SHIPPER' | 'CUSTOMER';
  status: 'ACTIVE' | 'BANNED';
  createdAt: string;
  updatedAt?: string;
}

// Shop types
export interface Shop {
  id: string;
  name: string;
  ownerId: string;
  ownerName?: string;
  ownerEmail?: string;
  status: 'OPEN' | 'CLOSED' | 'SUSPENDED' | 'BANNED' | 'PENDING_APPROVAL';
  rating: number;
  categoryId: string;
  categoryName?: string;
  logoUrl?: string;
  description?: string;
  address?: string;
  phone?: string;
  createdAt: string;
}

// Category types
export interface Category {
  id: string;
  name: string;
  nameEn?: string;
  slug: string;
  icon?: string;
  description?: string;
  sortOrder: number;
  isActive: boolean;
  productCount?: number;
  createdAt: string;
}

// Dashboard stats
export interface DashboardStats {
  users: {
    total: number;
    customers: number;
    owners: number;
    shippers: number;
    newToday: number;
  };
  shops: {
    total: number;
    active: number;
    pendingApproval: number;
  };
  orders: {
    today: number;
    thisWeek: number;
    thisMonth: number;
  };
  revenue: {
    today: number;
    thisWeek: number;
    thisMonth: number;
  };
  payouts: {
    pending: number;
    totalPendingAmount: number;
  };
}

// Payout types
export interface Payout {
  id: string;
  userId: string;
  userName?: string;
  userEmail?: string;
  userRole?: 'CUSTOMER' | 'OWNER' | 'SHIPPER' | 'ADMIN' | string;
  amount: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'TRANSFERRED';
  // Backend field names
  bankCode?: string;
  accountNumber?: string;
  accountName?: string;
  // Legacy/alternative field names (for backward compatibility)
  bankName?: string;
  bankAccountNumber?: string;
  bankAccountName?: string;
  reason?: string;
  transferNote?: string;
  requestedAt: string;
  processedAt?: string;
}

// API Response types
export interface ApiResponse<T = unknown> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
}

export interface PaginatedResponse<T> {
  success: boolean;
  data: T[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
    hasNext: boolean;
    hasPrev: boolean;
  };
}

// Query params
export interface ListUsersQuery {
  page?: number;
  limit?: number;
  role?: 'CUSTOMER' | 'OWNER' | 'SHIPPER';
  status?: 'ACTIVE' | 'BANNED';
  search?: string;
}

export interface ListShopsQuery {
  page?: number;
  limit?: number;
  status?: string;
  search?: string;
}

export interface ListPayoutsQuery {
  page?: number;
  limit?: number;
  status?: 'PENDING' | 'APPROVED' | 'REJECTED' | 'TRANSFERRED';
}
