import { Navigate } from "react-router";
import { userStore } from "../store/userStore";
import { Outlet } from "react-router";

type RoleRouteProps = {
  allowedRoles: string[];
  redirectPath: string;
};

export function RoleRoute({ allowedRoles, redirectPath }: RoleRouteProps) {
  const user = userStore((s) => s.user);
  if (user?.role) return <div>RoleRoute component</div>;
  if (!allowedRoles.includes(user?.role || "")) {
    Navigate({ to: redirectPath, replace: true });
  }
  return <Outlet />;
}
