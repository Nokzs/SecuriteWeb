
import { NavLink, useNavigate, useParams } from "react-router-dom";

export const BuildingNavBar = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    console.log("Déconnexion...");
    navigate("/login");
  };
  const {buildingId} = useParams();
  return (
    <nav className="w-64 h-screen flex flex-col bg-slate-50 border-r border-slate-200 p-6 shadow-sm">
      <div className="mb-10 px-2">
        <h1 className="text-xl font-bold text-indigo-600">Gestion Syndic</h1>
      </div>

      <div className="flex flex-col gap-2 flex-1">
        <NavLink 
          to={`/syndic/building/${buildingId}/appartments`} 
          className={({ isActive }) => 
            `flex items-center px-4 py-3 rounded-lg transition-colors ${
              isActive 
              ? "bg-indigo-600 text-white shadow-md" 
              : "text-slate-600 hover:bg-slate-200"
            }`
          }
        >
          appartements
        </NavLink>

        <NavLink 
          to={`/syndic/building/${buildingId}/residents`} 
          className={({ isActive }) => 
            `flex items-center px-4 py-3 rounded-lg transition-colors ${
              isActive 
              ? "bg-indigo-600 text-white shadow-md" 
              : "text-slate-600 hover:bg-slate-200"
            }`
          }
        >
          Résidents
        </NavLink>
      </div>

      <div className="border-t border-slate-200 pt-4">
        <button
          onClick={handleLogout}
          className="w-full flex items-center px-4 py-3 text-red-600 hover:bg-red-50 rounded-lg transition-colors font-medium"
        >
          <svg 
            xmlns="http://www.w3.org/2000/svg" 
            className="h-5 w-5 mr-3" 
            fill="none" 
            viewBox="0 0 24 24" 
            stroke="currentColor"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
          </svg>
          Déconnexion
        </button>
      </div>
    </nav>
  );
};
