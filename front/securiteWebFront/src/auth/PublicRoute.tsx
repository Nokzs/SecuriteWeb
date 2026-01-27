import { Navigate, Outlet } from "react-router-dom";
import { userStore } from "../store/userStore";

export function PublicRoute() {
  const user = userStore((s) => s.user);
  const get = userStore((s) => s.get);

  if (user) {
    const parsedUser = get(user);

    if (parsedUser?.role === "PROPRIETAIRE") {
      return <Navigate to="/owner" replace />;
    }
    if (parsedUser?.role === "SYNDIC") {
      return <Navigate to="/syndic" replace />;
    }
  }

  return <Outlet />;
}
