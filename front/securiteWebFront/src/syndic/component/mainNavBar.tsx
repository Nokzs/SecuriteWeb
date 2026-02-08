import { NavLink } from "react-router-dom";
import { NavBar } from "../../component/NavBar";
import { Mail, AlertTriangle, BarChart3 } from "lucide-react";

export const MainNavBarSyndic = () => {
  return (
    <NavBar>
      <div className="flex flex-col gap-2 flex-1">
        <NavLink
          to="/syndic/building"
          className={({ isActive }) =>
            `flex items-center px-4 py-3 rounded-lg transition-colors ${isActive
              ? "bg-indigo-600 text-white shadow-md"
              : "text-slate-600 hover:bg-slate-200"
            }`
          }
        >
          Immeubles
        </NavLink>

        <NavLink
          to="/syndic/messages"
          className={({ isActive }) =>
            `flex items-center gap-2 px-4 py-3 rounded-lg transition-colors ${isActive
              ? "bg-indigo-600 text-white shadow-md"
              : "text-slate-600 hover:bg-slate-200"
            }`
          }
        >
          <Mail size={18} />
          Boîte de réception
        </NavLink>

        <NavLink
          to="/syndic/reclamations"
          className={({ isActive }) =>
            `flex items-center gap-2 px-4 py-3 rounded-lg transition-colors ${isActive
              ? "bg-indigo-600 text-white shadow-md"
              : "text-slate-600 hover:bg-slate-200"
            }`
          }
        >
          <AlertTriangle size={18} />
          Réclamations
        </NavLink>

        <NavLink
          to="/syndic/votes"
          className={({ isActive }) =>
            `flex items-center gap-2 px-4 py-3 rounded-lg transition-colors ${isActive
              ? "bg-indigo-600 text-white shadow-md"
              : "text-slate-600 hover:bg-slate-200"
            }`
          }
        >
          <BarChart3 size={18} />
          Votes & Résultats
        </NavLink>
      </div>
    </NavBar >
  );
};
