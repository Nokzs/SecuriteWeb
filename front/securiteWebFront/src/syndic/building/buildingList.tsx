import { useState } from "react";
import { BuildingCard } from "./component/buildingCard";
import { Plus, Building2, Search } from "lucide-react";
import { userStore } from "../../store/userStore";
import { useSecureFetch } from "../../hooks/secureFetch";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import type { Page } from "../../types/pagination";
import { PaginationController } from "../../component/PaginationController";
import { AddBuildingPopUp } from "./addBuildingPopUp";
import { SearchBar } from "../component/SearchBar";
export interface Building {
  id: string;
  name: string;
  adresse: string;
  syndicId: string;
  photoFilename: string | null;
  totalTantieme: number | null;
  currentTantieme: number | null;
}
const API_URL = import.meta.env.VITE_APIURL;
export const BuildingsList = () => {
  const [showAddForm, setShowAddForm] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const secureFetch = useSecureFetch();
  const user = userStore((s) => s.user);
  const get = userStore((s) => s.get);
  const parsedUser = user ? get(user) : null;
  const [filter, setFilter] = useState<{ limit: number; page: number }>({
    limit: 5,
    page: 0,
  });
  const onPageChange = (newPage: number) => {
    setFilter((filter) => ({ ...filter, page: newPage }));
  };
  const { data, isLoading } = useQuery<Page<Building>>({
    queryKey: ["buildings", parsedUser?.uuid, filter, searchTerm],
    queryFn: async () => {
      const res = await secureFetch(
        `${API_URL}/building?limit=${filter.limit}&page=${filter.page}&search=${encodeURIComponent(searchTerm)}`,
      );

      if (!res.ok) {
        throw new Error("Erreur lors de la récupération des bâtiments");
      }

      return (await res.json()) as Page<Building>;
    },
    enabled: !!parsedUser?.uuid,
    placeholderData: keepPreviousData,
  });
  if (isLoading) {
    return <span className="loading loading-spinner loading-lg"></span>;
  }
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
      <div className="mb-6 relative">
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <Search className="text-slate-400" size={18} />
        </div>
        <SearchBar
          searchTerm={searchTerm}
          setFilter={setFilter}
          setSearchTerm={setSearchTerm}
        />
      </div>
      {showAddForm && <AddBuildingPopUp setShowAddForm={setShowAddForm} />}{" "}
      <div className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {data &&
            !isLoading &&
            data.content.map((building: Building) => (
              <BuildingCard building={building} key={building.id} />
            ))}
        </div>
        {data && !isLoading && (
          <PaginationController<Building>
            onPageChange={onPageChange}
            pageData={data}
          />
        )}
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
