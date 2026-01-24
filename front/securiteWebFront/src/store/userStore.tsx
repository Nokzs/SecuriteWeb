

import { create } from 'zustand';
import { persist } from 'zustand/middleware';

type User = {
  uuid: string;
  role: string;
};

export type UserStoreType = {
  user: User | null;
  setUser: (userData: User) => void;
  clearUser: () => void;
};

export const userStore = create<UserStoreType>()(
  persist(
    (set) => ({
      user: null,

      setUser: (userData) => set({ user: userData }),

      clearUser: () => {
        set({ user: null });
        localStorage.removeItem('auth-storage');
      },
    }),
    {
      name: 'auth-storage',
    }
  )
);
