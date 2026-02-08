import { NavLink, useParams } from "react-router-dom";
import { StepBack } from "lucide-react";
import { NavBar } from "../../component/NavBar";
export const BuildingNavBar = () => {
  const { buildingId } = useParams();
  return (
    <NavBar>
      <div className="flex flex-col gap-2 flex-1">
        <NavLink
          to="/syndic/building"
          className="flex items-center px-4 py-3 rounded-lg text-slate-600 hover:bg-slate-200 transition-colors"
        >
          <StepBack className="mr-3" />
        </NavLink>
        <NavLink
          to={`/syndic/building/${buildingId}/apartments`}
          className={({ isActive }) =>
            `flex items-center px-4 py-3 rounded-lg transition-colors ${
              isActive
                ? "bg-indigo-600 text-white shadow-md"
                : "text-slate-600 hover:bg-slate-200"
            }`
          }
        >
          Appartements
        </NavLink>

        <NavLink
          to={`/syndic/building/${buildingId}/expenses`}
          className={({ isActive }) =>
            `flex items-center px-4 py-3 rounded-lg transition-colors ${
              isActive
                ? "bg-indigo-600 text-white shadow-md"
                : "text-slate-600 hover:bg-slate-200"
            }`
          }
        >
          Factures
        </NavLink>
      </div>
    </NavBar>
  );
};
