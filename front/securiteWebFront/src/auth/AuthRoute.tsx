import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useState, useEffect } from "react";
import { LOGIN_URL, API_BASE } from "../config/urls";
import { userStore, type User } from "../store/userStore";
import { da } from "zod/locales";

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

  // 1. Gestion PROPRIETAIRE
  if (role === "PROPRIETAIRE" || roles.includes("ROLE_PROPRIETAIRE")) {
    return {
      uuid: gatewayUser.sub,
      role: "PROPRIETAIRE",
      isFirstLogin: gatewayUser.isFirstLogin,
      authenticated: true,
    };
  }

  // 2. Gestion SYNDIC
  if (role === "SYNDIC" || roles.includes("ROLE_SYNDIC")) {
    return {
      uuid: gatewayUser.sub,
      role: "SYNDIC",
      authenticated: true,
      isFirstLogin: false,
    };
  }

  // 3. Gestion ADMIN
  if (role === "ADMIN" || roles.includes("ROLE_ADMIN")) {
    return {
      uuid: gatewayUser.sub,
      role: "ADMIN",
      authenticated: true,
      isFirstLogin: false,
    };
  }

  // Fallback si aucun rôle connu
  return { uuid: gatewayUser.sub, role: "", authenticated: true };
};

export function AuthRoute() {
  const { user, setUser } = userStore();
  const [loading, setLoading] = useState(!user);
  const location = useLocation();

  useEffect(() => {
    if (user) return;
    fetch(`${API_BASE}/user/me`, { credentials: "include" })
      .then((res) => (res.ok ? res.json() : null))
      .then((data) => {
        if (data) {
          setUser(toAppUser(data));
          console.log(data);
        }
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  if (loading) return null;
  if (!user) {
    window.location.assign(LOGIN_URL);
    return null;
  }

  const isFirstLoginPath = location.pathname === "/owner/first-login";

  if (user.role === "PROPRIETAIRE" && user.isFirstLogin) {
    if (!isFirstLoginPath) return <Navigate to="/owner/first-login" replace />;
  } else if (isFirstLoginPath) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
