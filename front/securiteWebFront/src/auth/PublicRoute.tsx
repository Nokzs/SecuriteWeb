import { Navigate, Outlet } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";

import { API_BASE, GATEWAY_BASE } from "../config/urls";
import { userStore, type User } from "../store/userStore";
import { useEffect } from "react";

type GatewayUser = {
  authenticated?: boolean;
  roles?: string[];
  sub?: string;
  role?: string;
  isFirstLogin?: boolean;
};

const toAppUser = (gatewayUser: GatewayUser): User | null => {
  if (!gatewayUser?.authenticated) return null;

  const roles = gatewayUser.roles ?? [];
  const role = gatewayUser.role;

  if (role === "PROPRIETAIRE" || roles.includes("ROLE_PROPRIETAIRE")) {
    return {
      uuid: gatewayUser.sub,
      role: "PROPRIETAIRE",
      isFirstLogin: gatewayUser.isFirstLogin,
      authenticated: true,
    };
  }

  if (role === "SYNDIC" || roles.includes("ROLE_SYNDIC")) {
    return {
      uuid: gatewayUser.sub,
      role: "SYNDIC",
      authenticated: true,
    };
  }

  if (role === "ADMIN" || roles.includes("ROLE_ADMIN")) {
    return {
      uuid: gatewayUser.sub,
      role: "ADMIN",
      authenticated: true,
    };
  }

  return { uuid: gatewayUser.sub, role: "", authenticated: true };
};

export function PublicRoute() {
  const user = userStore((s) => s.user);
  const get = userStore((s) => s.get);
  const setUser = userStore((s) => s.setUser);

  const query = useQuery({
    queryKey: ["gatewayUser"],
    queryFn: async (): Promise<User | null> => {
      const response = await fetch(`${API_BASE}/user/me`, {
        method: "GET",
        credentials: "include",
      });
      if (!response.ok) return null;
      const gatewayUser = (await response.json()) as GatewayUser;
      return toAppUser(gatewayUser);
    },
    retry: false,
    staleTime: 15_000,
  });

  useEffect(() => {
    if (query.isSuccess) {
      const currentUser = get(user);
      const nextUser = query.data;

      if (
        currentUser?.role !== nextUser?.role ||
        currentUser?.isFirstLogin !== nextUser?.isFirstLogin ||
        currentUser?.uuid !== nextUser?.uuid
      ) {
        setUser(nextUser);
      }
    }
  }, [query.isSuccess, query.data, user, get, setUser]); // Se déclenche seulement quand query change

  if (query.isLoading) return null;

  // Utiliser directement query.data ou le store mis à jour
  const parsedUser = query.data;

  const isInternalPage =
    location.pathname.startsWith("/syndic") ||
    location.pathname.startsWith("/owner") ||
    location.pathname.startsWith("/admin");

  if (parsedUser && !isInternalPage) {
    if (parsedUser.role === "SYNDIC") {
      return <Navigate to="/syndic" replace />;
    }

    if (parsedUser.role === "PROPRIETAIRE") {
      if (parsedUser.isFirstLogin) {
        return <Navigate to="/owner/first-login" replace />;
      }
      return <Navigate to="/owner" replace />;
    }

    if (parsedUser.role === "ADMIN") {
      return <Navigate to="/admin" replace />;
    }
  }

  return <Outlet />;
}
