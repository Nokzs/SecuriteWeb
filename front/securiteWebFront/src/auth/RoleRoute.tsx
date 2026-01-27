import { Navigate, Outlet } from "react-router-dom";
import { userStore } from "../store/userStore";

type RoleRouteProps = {
  allowedRoles: string[];
  redirectPath: string;
};

export function RoleRoute({ allowedRoles, redirectPath }: RoleRouteProps) {
  const user = userStore((s) => s.user);
  const get = userStore((s) => s.get);
  const parsedUser = user ? get(user) : null;

  if (!parsedUser?.role) {
    return <Navigate to="/" replace />;
  }

  if (!allowedRoles.includes(parsedUser.role)) {
    return <Navigate to={redirectPath} replace />;
  }

  return <Outlet />;
}
