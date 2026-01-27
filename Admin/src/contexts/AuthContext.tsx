import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { signInWithEmailAndPassword, signOut, onAuthStateChanged } from 'firebase/auth';
import { auth } from '../config/firebase';
import api, { setAuthToken } from '../api/client';
import { User } from '../types';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  // Check if user is logged in on mount
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (firebaseUser) => {
      if (firebaseUser) {
        try {
          // Get fresh ID token
          const idToken = await firebaseUser.getIdToken();
          setAuthToken(idToken);

          // Verify admin role via backend
          const response = await api.get<{ user: User }>('/me');
          const userData = response.data.user;

          // Critical: Check if user is ADMIN
          if (userData.role !== 'ADMIN') {
            console.error('Access denied: User is not an admin');
            await signOut(auth);
            setAuthToken(null);
            setUser(null);
            setLoading(false);
            return;
          }

          // Store user data
          localStorage.setItem('admin_user', JSON.stringify(userData));
          setUser(userData);
        } catch (error) {
          console.error('Failed to verify admin role:', error);
          await signOut(auth);
          setAuthToken(null);
          setUser(null);
        }
      } else {
        setAuthToken(null);
        setUser(null);
        localStorage.removeItem('admin_user');
      }
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const login = async (email: string, password: string) => {
    try {
      // 1. Sign in with Firebase
      const userCredential = await signInWithEmailAndPassword(auth, email, password);

      // 2. Get ID token
      const idToken = await userCredential.user.getIdToken();
      setAuthToken(idToken);

      // 3. Verify admin role (call backend /me)
      const response = await api.get<{ user: User }>('/me');
      const userData = response.data.user;

      // 4. Check if user is admin - CRITICAL CHECK
      if (userData.role !== 'ADMIN') {
        await signOut(auth);
        setAuthToken(null);
        throw new Error('Access denied. Admin role required.');
      }

      // 5. Store token and user data
      localStorage.setItem('admin_user', JSON.stringify(userData));
      setUser(userData);
    } catch (error: unknown) {
      console.error('Login error:', error);
      setAuthToken(null);
      throw error;
    }
  };

  const logout = async () => {
    await signOut(auth);
    setAuthToken(null);
    localStorage.removeItem('admin_user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
