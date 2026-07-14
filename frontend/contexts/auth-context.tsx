'use client';

import * as React from 'react';
import { useRouter } from 'next/navigation';
import Cookies from 'js-cookie';
import { toast } from 'sonner';

import { authService } from '@/services/auth.service';
import { extractApiError } from '@/services/api';
import { STORAGE_KEYS } from '@/constants/app';
import type { User, UserRole, LoginResponse, AuthResponse } from '@/types';

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string, remember?: boolean) => Promise<User>;
  register: (input: {
    fullName: string;
    email: string;
    password: string;
    phone: string;
    gender: string;
    birthDate: string;
  }) => Promise<User | AuthResponse>;
  logout: () => void;
  updateUser: (patch: Partial<User>) => void;
  hasRole: (...roles: UserRole[]) => boolean;
}

const AuthContext = React.createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = React.useState<User | null>(null);
  const [loading, setLoading] = React.useState(true);
  const router = useRouter();

  // Khôi phục session khi ứng dụng khởi chạy
  React.useEffect(() => {
    let active = true;

    (async () => {
      try {
        const token = Cookies.get(STORAGE_KEYS.accessToken);
        if (!token) {
          setLoading(false);
          return;
        }

        const me = await authService.me();
        if (active) {
          setUser(me);
          Cookies.set(STORAGE_KEYS.user, JSON.stringify(me), { expires: 7 });
        }
      } catch (err) {
        console.error('Restore session error:', err);
      } finally {
        if (active) setLoading(false);
      }
    })();

    return () => {
      active = false;
    };
  }, []);

  // Hàm helper lưu thông tin token và user vào Cookies
  const persist = React.useCallback((
    tokens: { accessToken: string; refreshToken: string },
    u: User
  ) => {
    Cookies.set(STORAGE_KEYS.accessToken, tokens.accessToken, { expires: 7 });
    Cookies.set(STORAGE_KEYS.refreshToken, tokens.refreshToken, { expires: 30 });
    Cookies.set(STORAGE_KEYS.user, JSON.stringify(u), { expires: 7 });
    setUser(u);
  }, []);

  // Hàm Đăng nhập
  const login = React.useCallback(async (email: string, password: string) => {
    try {
      const response = await authService.login(email, password);

      // Handle both LoginResponse (real API) and AuthResponse (mock)
      let tokens: { accessToken: string; refreshToken: string; expiresIn: number };
      if ('tokens' in response) {
        // AuthResponse (mock)
        tokens = response.tokens;
      } else {
        // LoginResponse (real API)
        tokens = {
          accessToken: response.accessToken,
          refreshToken: response.refreshToken,
          expiresIn: Math.floor((response.accessTokenExpiresAt - Date.now()) / 1000),
        };
      }

      // Đặt token tạm thời để gọi được API lấy thông tin cá nhân (me)
      Cookies.set(STORAGE_KEYS.accessToken, tokens.accessToken, { expires: 7 });
      Cookies.set(STORAGE_KEYS.refreshToken, tokens.refreshToken, { expires: 30 });

      const me = await authService.me();
      persist(tokens, me);

      toast.success(`Chào mừng trở lại, ${me.fullName}!`);
      return me;
    } catch (err) {
      toast.error(extractApiError(err, 'Thông tin đăng nhập không chính xác'));
      throw err;
    }
  }, [persist]);

  // Hàm Đăng ký
  const register = React.useCallback(async (input: {
    fullName: string;
    email: string;
    password: string;
    phone: string;
    gender: string;
    birthDate: string;
  }) => {
    try {
      const res = await authService.register(input);
      toast.success('Tạo tài khoản thành công');
      return res;
    } catch (err) {
      toast.error(extractApiError(err, 'Đăng ký tài khoản thất bại'));
      throw err;
    }
  }, []);

  // Hàm Đăng xuất
  const logout = React.useCallback(async () => {
    const refreshToken = Cookies.get(STORAGE_KEYS.refreshToken);
    
    try {
      await authService.logout(refreshToken ?? '');
    } catch (error) {
      // Ignore 401 errors - token may already be expired
      console.warn('Logout API call failed:', error);
    }

    Cookies.remove(STORAGE_KEYS.accessToken);
    Cookies.remove(STORAGE_KEYS.refreshToken);
    Cookies.remove(STORAGE_KEYS.user);

    setUser(null);
    toast.success('Đăng xuất thành công');
    router.push('/');
  }, [router]);

  // Hàm cập nhật thông tin User (Local state & Cookie)
  const updateUser = React.useCallback((patch: Partial<User>) => {
    setUser((prev) => {
      if (!prev) return prev;
      const next = { ...prev, ...patch };
      Cookies.set(STORAGE_KEYS.user, JSON.stringify(next), { expires: 7 });
      return next;
    });
  }, []);

  // Kiểm tra quyền (Role)
  const hasRole = React.useCallback((...roles: UserRole[]) => {
    return user ? roles.includes(user.roleName as UserRole) : false;
  }, [user]);

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        isAuthenticated: !!user,
        login,
        register,
        logout,
        updateUser,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = React.useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}