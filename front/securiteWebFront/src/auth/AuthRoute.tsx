import { Navigate, Outlet, useLocation } from "react-router-dom";
import { userStore } from "../store/userStore";

export function AuthRoute() {
  const user = userStore((s) => s.user);
  const get = userStore((s) => s.get);
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  const parsedUser = get(user);
  if (!parsedUser) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (
    parsedUser.role === "PROPRIETAIRE" &&
    parsedUser.isFirstLogin &&
    location.pathname !== "/owner/first-login"
  ) {
    return <Navigate to="/owner/first-login" replace />;
  }

  if (
    parsedUser.role !== "PROPRIETAIRE" &&
    location.pathname === "/owner/first-login"
  ) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
