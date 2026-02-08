import { NavLink } from "react-router-dom";
import { NavBar } from "../component/NavBar";
import { Home, AlertCircle, Receipt } from "lucide-react";

export const MainNavBarOwner = () => {
  return (
    <NavBar>
      <div className="flex flex-col gap-2 flex-1">
        <NavLink
          to="/owner/properties"
          className={({ isActive }) =>
            `flex items-center gap-2 px-4 py-3 rounded-lg transition-colors ${
              isActive
                ? "bg-indigo-600 text-white shadow-md"
                : "text-slate-600 hover:bg-slate-200"
            }`
          }
        >
          <Home size={18} />
          Mes Logements
        </NavLink>

        {/* Tu pourras ajouter cette page plus tard pour voir l'historique */}
        <NavLink
          to="/owner/incidents"
          className={({ isActive }) =>
            `flex items-center gap-2 px-4 py-3 rounded-lg transition-colors ${
              isActive
                ? "bg-indigo-600 text-white shadow-md"
                : "text-slate-600 hover:bg-slate-200"
            }`
          }
        >
          <AlertCircle size={18} />
          Mes Signalements{" "}
        </NavLink>
        <NavLink
          to="/owner/invoices"
          className={({ isActive }) =>
            `flex items-center gap-2 px-4 py-3 rounded-lg transition-colors ${
              isActive
                ? "bg-indigo-600 text-white shadow-md"
                : "text-slate-600 hover:bg-slate-200"
            }`
          }
        >
          <Receipt size={18} />
          Mes demande de payements
        </NavLink>
      </div>
    </NavBar>
  );
};
