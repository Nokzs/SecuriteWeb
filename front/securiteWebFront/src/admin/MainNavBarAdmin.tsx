import { NavLink } from "react-router-dom";
import { NavBar } from "../component/NavBar"; // Assure-toi que le chemin est bon
import { LayoutDashboard, ShieldAlert } from "lucide-react";

export const MainNavBarAdmin = () => {
  return (
    <NavBar>
      <div className="flex flex-col gap-2 flex-1">
        
        {/* LIEN 1 : TABLEAU DE BORD */}
        <NavLink
          to="/admin"
          end // Important pour ne pas qu'il reste actif sur les sous-routes éventuelles
          className={({ isActive }) =>
            `flex items-center gap-2 px-4 py-3 rounded-lg transition-colors ${
              isActive
                ? "bg-indigo-600 text-white shadow-md"
                : "text-slate-600 hover:bg-slate-200"
            }`
          }
        >
          <LayoutDashboard size={18} />
          Vue d'ensemble
        </NavLink>

        <div className="mt-4 px-4 text-xs font-bold text-slate-400 uppercase tracking-wider">
            Sécurité
        </div>
        
        <NavLink
          to="/admin/logs"
          className={({ isActive }) =>
            `flex items-center gap-2 px-4 py-3 rounded-lg transition-colors ${
              isActive
                ? "bg-indigo-600 text-white shadow-md"
                : "text-slate-600 hover:bg-slate-200"
            }`
          }
        >
          <ShieldAlert size={18} />
          Logs & Audit
        </NavLink>

      </div>
    </NavBar>
  );
};