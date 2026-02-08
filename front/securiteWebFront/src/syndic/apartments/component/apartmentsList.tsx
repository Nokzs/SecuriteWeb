import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { Building2, Plus, Search } from "lucide-react";
import type { Page } from "../../../types/pagination";
import { useState } from "react";
import { useSecureFetch } from "../../../hooks/secureFetch";
import { userStore } from "../../../store/userStore";
import { useParams } from "react-router";
import { PaginationController } from "../../../component/PaginationController";
import { SearchBar } from "../../component/SearchBar";
import type { Building } from "../../building/buildingList";
import { AddApartmentPopUp } from "./addApartmentPopUp";
import { ApartmentCard } from "./ApartmentCard";

type ApartementAndBuildingDto = {
  building: Building;
  appartement: Page<Apartment>;
};

export interface Apartment {
  id: string;
  numero: string; // ex: "A101"
  surface?: number; // ex: 75.5
  etage?: number;
  nombrePieces?: number;
  photoFilename: string | null;
  signedLink?: string | null;
  tantiemes: number;
  ownerId?: string;
  buildingUuid: string;
}

const API_URL = import.meta.env.VITE_APIURL;

export const ApartmentList = () => {
  const [showAddForm, setShowAddForm] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const { buildingId } = useParams<{ buildingId: string }>();
  const [showEditModal, setShowEditModal] = useState(false);
  const [apartmentToEdit, setApartmentToEdit] = useState<Apartment | null>(null);
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

  const { data, isLoading } = useQuery<ApartementAndBuildingDto>({
    queryKey: ["apartments", parsedUser?.uuid, buildingId, filter, searchTerm],
    queryFn: async () => {
      const res = await secureFetch(
        `${API_URL}/apartment/${buildingId}?limit=${filter.limit}&page=${filter.page}&search=${encodeURIComponent(searchTerm)}`,
      );

      if (!res.ok) {
        throw new Error("Erreur lors de la récupération des appartements");
      }

      return (await res.json()) as ApartementAndBuildingDto;
    },
    enabled: !!parsedUser?.uuid && !!buildingId,
    placeholderData: keepPreviousData,
  });

  if (isLoading) {
    return <span className="loading loading-spinner loading-lg"></span>;
  }

  const openEditModal = (apartment: Apartment) => {
    setApartmentToEdit(apartment);
    setShowEditModal(true);
    setShowAddForm(false);
  };

  return (
    <div className="max-w-6xl mx-auto p-6">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">
            Mes Appartements
          </h1>
          <p className="text-slate-500">
            Consultez les appartements de cet immeuble
          </p>
        </div>

        <button
          onClick={() => setShowAddForm(!showAddForm)}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg shadow-sm transition-all"
        >
          <Plus size={20} />
          Nouvel Appartement
        </button>
      </div>

      {showAddForm && (
        <AddApartmentPopUp
          setShowAddForm={setShowAddForm}
          building={data?.building}
        />
      )}

      {showEditModal && apartmentToEdit && (
        <AddApartmentPopUp
          setShowAddForm={setShowEditModal}
          building={data?.building}
          apartmentToEdit={apartmentToEdit}
        />
      )}

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

      <div className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {data &&
            !isLoading &&
            data.appartement.content.map((apartment: Apartment) => (
              <ApartmentCard
                key={apartment.id}
                apartment={apartment}
                building={data.building}
                openModal={openEditModal}
              />
            ))}
        </div>

        {data && !isLoading && (
          <PaginationController<Apartment>
            onPageChange={onPageChange}
            pageData={data.appartement}
          />
        )}
      </div>

      {data && data.appartement.content.length === 0 && !showAddForm && (
        <div className="text-center py-20 bg-slate-50 rounded-2xl border-2 border-dashed border-slate-200">
          <Building2 className="mx-auto text-slate-300 mb-4" size={48} />
          <p className="text-slate-500">Aucun appartement trouvé.</p>
        </div>
      )}
    </div>
  );
};
