import { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';
import {
  collection,
  query,
  where,
  onSnapshot,
  orderBy,
  limit,
  Unsubscribe,
} from 'firebase/firestore';
import { db } from '../config/firebase';
import { useAuth } from './AuthContext';
import { notification } from 'antd';

/**
 * RealtimeContext - Firestore Realtime Listeners for Admin Panel
 *
 * Provides:
 * - pendingPayoutsCount: Number of PENDING payout requests
 * - pendingShopsCount: Number of shops waiting for approval
 * - recentPayouts: Latest 5 pending payouts (for quick access)
 * - recentPendingShops: Latest 5 pending shops
 * - Auto-notification when new items arrive
 */

interface RealtimeData {
  // Counts for badges
  pendingPayoutsCount: number;
  pendingShopsCount: number;

  // Recent items for quick preview
  recentPayouts: PayoutItem[];
  recentPendingShops: ShopItem[];

  // Loading states
  isLoading: boolean;

  // Manual refresh function
  triggerRefresh: () => void;
}

interface PayoutItem {
  id: string;
  userId: string;
  userName?: string;
  amount: number;
  status: string;
  requestedAt: Date;
}

interface ShopItem {
  id: string;
  name: string;
  ownerName?: string;
  status: string;
  createdAt: Date;
}

const RealtimeContext = createContext<RealtimeData | undefined>(undefined);

export function RealtimeProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [pendingPayoutsCount, setPendingPayoutsCount] = useState(0);
  const [pendingShopsCount, setPendingShopsCount] = useState(0);
  const [recentPayouts, setRecentPayouts] = useState<PayoutItem[]>([]);
  const [recentPendingShops, setRecentPendingShops] = useState<ShopItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [refreshKey, setRefreshKey] = useState(0);

  // Track previous counts to detect new items
  const [prevPayoutsCount, setPrevPayoutsCount] = useState<number | null>(null);
  const [prevShopsCount, setPrevShopsCount] = useState<number | null>(null);

  const triggerRefresh = useCallback(() => {
    setRefreshKey((k) => k + 1);
  }, []);

  useEffect(() => {
    // Only listen if user is authenticated as admin
    if (!user || user.role !== 'ADMIN') {
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    const unsubscribes: Unsubscribe[] = [];

    // ========================================
    // 1. Listen to PENDING payouts
    // ========================================
    try {
      const payoutsQuery = query(
        collection(db, 'payoutRequests'),
        where('status', '==', 'PENDING'),
        orderBy('requestedAt', 'desc'),
        limit(50) // Limit for performance
      );

      const unsubPayouts = onSnapshot(
        payoutsQuery,
        (snapshot) => {
          const count = snapshot.size;
          const items: PayoutItem[] = snapshot.docs.map((doc) => {
            const data = doc.data();
            return {
              id: doc.id,
              userId: data.userId,
              userName: data.userName,
              amount: data.amount,
              status: data.status,
              requestedAt: data.requestedAt?.toDate() || new Date(),
            };
          });

          // Check for new payouts (only after initial load)
          if (prevPayoutsCount !== null && count > prevPayoutsCount) {
            const newCount = count - prevPayoutsCount;
            notification.info({
              message: 'Yêu cầu rút tiền mới',
              description: `Có ${newCount} yêu cầu rút tiền mới cần duyệt`,
              placement: 'topRight',
              duration: 5,
            });
          }

          setPrevPayoutsCount(count);
          setPendingPayoutsCount(count);
          setRecentPayouts(items.slice(0, 5));
        },
        (error) => {
          console.error('Error listening to payouts:', error);
        }
      );
      unsubscribes.push(unsubPayouts);
    } catch (error) {
      console.error('Failed to setup payouts listener:', error);
    }

    // ========================================
    // 2. Listen to PENDING_APPROVAL shops
    // ========================================
    try {
      const shopsQuery = query(
        collection(db, 'shops'),
        where('status', '==', 'PENDING_APPROVAL'),
        orderBy('createdAt', 'desc'),
        limit(50)
      );

      const unsubShops = onSnapshot(
        shopsQuery,
        (snapshot) => {
          const count = snapshot.size;
          const items: ShopItem[] = snapshot.docs.map((doc) => {
            const data = doc.data();
            return {
              id: doc.id,
              name: data.name,
              ownerName: data.ownerName,
              status: data.status,
              createdAt: data.createdAt?.toDate() || new Date(),
            };
          });

          // Check for new pending shops
          if (prevShopsCount !== null && count > prevShopsCount) {
            const newCount = count - prevShopsCount;
            notification.info({
              message: 'Shop mới cần duyệt',
              description: `Có ${newCount} shop mới đang chờ phê duyệt`,
              placement: 'topRight',
              duration: 5,
            });
          }

          setPrevShopsCount(count);
          setPendingShopsCount(count);
          setRecentPendingShops(items.slice(0, 5));
        },
        (error) => {
          console.error('Error listening to shops:', error);
        }
      );
      unsubscribes.push(unsubShops);
    } catch (error) {
      console.error('Failed to setup shops listener:', error);
    }

    setIsLoading(false);

    // Cleanup listeners on unmount
    return () => {
      unsubscribes.forEach((unsub) => unsub());
    };
  }, [user, refreshKey]);

  return (
    <RealtimeContext.Provider
      value={{
        pendingPayoutsCount,
        pendingShopsCount,
        recentPayouts,
        recentPendingShops,
        isLoading,
        triggerRefresh,
      }}
    >
      {children}
    </RealtimeContext.Provider>
  );
}

export function useRealtime() {
  const context = useContext(RealtimeContext);
  if (!context) {
    throw new Error('useRealtime must be used within a RealtimeProvider');
  }
  return context;
}
