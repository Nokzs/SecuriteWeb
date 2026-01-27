import { create } from "zustand";
import { persist } from "zustand/middleware";

type User = {
  uuid: string;
  role: string;
  isFirstLogin: boolean;
};

export type UserStoreType = {
  user: string | null;
  setToken: (userData: string) => void;
  get(token: string): User | null;
  clearUser: () => void;
};

export const userStore = create<UserStoreType>()(
  persist(
    (set) => ({
      get: (access?: string | null) => {
        if (!access) return null;
        try {
          const parts = access.split(".");
          if (parts.length < 2) return null;
          const accessPayload: User = JSON.parse(atob(parts[1]));
          return {
            uuid: accessPayload.uuid,
            role: accessPayload.role,
            isFirstLogin: accessPayload.isFirstLogin,
          };
        } catch (e: unknown) {
          if (e instanceof Error) {
            console.error("Error parsing access token:", e.message);
          }
          return null;
        }
      },
      user: null,

      setToken: (userData) => set({ user: userData }),

      clearUser: () => {
        set({ user: null });
        localStorage.removeItem("auth-storage");
      },
    }),
    {
      name: "auth-storage",
    },
  ),
);
