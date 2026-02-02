import { NavLink } from "react-router-dom";
import { NavBar } from "../../component/NavBar";

export const MainNavBarSyndic = () => {
  return (
    <NavBar>
      <div className="flex flex-col gap-2 flex-1">
        <NavLink
          to="/syndic/building"
          className={({ isActive }) =>
            `flex items-center px-4 py-3 rounded-lg transition-colors ${
              isActive
                ? "bg-indigo-600 text-white shadow-md"
                : "text-slate-600 hover:bg-slate-200"
            }`
          }
        >
          Immeubles
        </NavLink>

        <NavLink
          to="/syndic/reclamations"
          className={({ isActive }) =>
            `flex items-center px-4 py-3 rounded-lg transition-colors ${
              isActive
                ? "bg-indigo-600 text-white shadow-md"
                : "text-slate-600 hover:bg-slate-200"
            }`
          }
        >
          Réclamations
        </NavLink>
      </div>
    </NavBar>
  );
};
