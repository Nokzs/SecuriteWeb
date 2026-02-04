import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";

import { LOGIN_URL, API_BASE } from "../config/urls";
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

  return { uuid: gatewayUser.sub, role: "", authenticated: true };
};

export function AuthRoute() {
  const user = userStore((s) => s.user);
  const get = userStore((s) => s.get);
  const setUser = userStore((s) => s.setUser);
  const location = useLocation();

  const query = useQuery({
    queryKey: ["gatewayUser"],
    queryFn: async (): Promise<User | null> => {
      const response = await fetch(`${API_BASE}/auth/user`, {
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
  }, [query.isSuccess, query.data, user, get, setUser]);

  if (query.isLoading) {
    return null; // Ou un composant de chargement
  }

  // On utilise la donnée fraîche de la query pour la logique de rendu
  const parsedUser = query.data;

  // 1. Redirection vers le SSO si non authentifié
  if (!parsedUser) {
    window.location.assign(LOGIN_URL);
    return null;
  }
  // On ne redirige QUE si on est sur la racine "/" ou la page "/login"
  const isInternalPage =
    location.pathname.startsWith("/syndic") ||
    location.pathname.startsWith("/owner");

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
  }
  // 2. Gestion du flag First Login pour PROPRIETAIRE
  // On vérifie si l'utilisateur DOIT changer son mdp et n'est pas déjà sur la bonne page
  if (
    parsedUser.role === "PROPRIETAIRE" &&
    parsedUser.isFirstLogin &&
    location.pathname !== "/owner/first-login"
  ) {
    return <Navigate to="/owner/first-login" replace />;
  }

  // 3. Empêcher l'accès à la page First Login si le flag est false ou le rôle incorrect
  if (
    location.pathname === "/owner/first-login" &&
    (!parsedUser.isFirstLogin || parsedUser.role !== "PROPRIETAIRE")
  ) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
