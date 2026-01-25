import { Navigate, Outlet, useLocation } from "react-router-dom";
import { userStore } from "../store/userStore";
export function PublicRoute() {
  const user = userStore((s) => s.user);
  const location = useLocation();

  if (user) {
    if (user.role === "PROPRIETAIRE") {
      return <Navigate to="/owner" state={{ from: location }} replace />;
    }
    if (user.role === "SYNDIC") {
      return <Navigate to="/syndic" state={{ from: location }} replace />;
    }
  }
  return <Outlet />;
}
