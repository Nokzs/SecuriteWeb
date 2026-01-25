import { useState } from "react";
import { Plus, Building2, MapPin, ChevronRight } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { userStore } from "../../store/userStore";
import { useSecureFetch } from "../../hooks/secureFetch";
import { useQuery } from "@tanstack/react-query";
import { AddBuildingPopUp } from "../component/addBuildingPopUp";
import type { Page } from "../../types/pagination";
export interface Building {
  id: string;
  name: string;
  address: string;
  syndicId: string;
  photoFilename: string | null;
}
const API_URL = import.meta.env.VITE_APIURL;
export const BuildingsList = () => {
  const [showAddForm, setShowAddForm] = useState(false);
  const secureFetch = useSecureFetch();
  const user = userStore((s) => s.user);
  const navigate = useNavigate();
  const { data, isLoading } = useQuery<Page<Building>>({
    queryKey: ["buildings", user?.uuid],
    queryFn: async () => {
      const res = await secureFetch(`${API_URL}/building`);

      if (!res.ok) {
        throw new Error("Erreur lors de la récupération des bâtiments");
      }

      return (await res.json()) as Page<Building>;
    },
    enabled: !!user?.uuid,
  });
  console.log("Buildings data:", data);
  return (
    <div className="max-w-6xl mx-auto p-6">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Mes Immeubles</h1>
          <p className="text-slate-500">
            Gérez le parc immobilier de votre syndic
          </p>
        </div>

        <button
          onClick={() => setShowAddForm(!showAddForm)}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg shadow-sm transition-all"
        >
          <Plus size={20} />
          Nouveau Bâtiment
        </button>
      </div>
      {showAddForm && <AddBuildingPopUp setShowAddForm={setShowAddForm} />}{" "}
      {/* Liste des b�timents */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {data &&
          !isLoading &&
          data?.content.map((building: Building) => (
            <div
              key={building.id}
              onClick={() =>
                navigate(`/syndic/building/${building.id}/apartments`)
              }
              className="group cursor-pointer bg-white border border-slate-200 p-5 rounded-xl hover:border-indigo-400 hover:shadow-md transition-all relative overflow-hidden"
            >
              <div className="flex items-start justify-between">
                <div className="p-3 bg-indigo-50 text-indigo-600 rounded-lg group-hover:bg-indigo-600 group-hover:text-white transition-colors">
                  <Building2 size={24} />
                </div>
                <ChevronRight className="text-slate-300 group-hover:text-indigo-500 transition-colors" />
              </div>

              <h3 className="mt-4 font-bold text-lg text-slate-800">
                {building.name}
              </h3>
              <div className="flex items-center gap-1 text-slate-500 text-sm mt-1">
                <MapPin size={14} />
                <p>{building.address}</p>
              </div>
            </div>
          ))}
      </div>
      {data && data.content.length === 0 && !showAddForm && (
        <div className="text-center py-20 bg-slate-50 rounded-2xl border-2 border-dashed border-slate-200">
          <Building2 className="mx-auto text-slate-300 mb-4" size={48} />
          <p className="text-slate-500">
            Aucun bâtiment enregistré pour le moment.
          </p>
        </div>
      )}
    </div>
  );
};
