import { Navigate, Outlet, useLocation } from "react-router-dom";
import { userStore } from "../store/userStore";
export function AuthRoute() {
  const user = userStore((s) => s.user);
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <Outlet />;
}
