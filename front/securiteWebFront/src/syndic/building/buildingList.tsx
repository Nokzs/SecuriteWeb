import { useState } from "react";
import { Plus, Building2, MapPin, ChevronRight } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { userStore } from "../../store/userStore";
import { useSecureFetch } from "../../hooks/secureFetch";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
interface Building {
  id: string;
  name: string;
  address: string;
  apartmentCount: number;
}
const API_URL = import.meta.env.VITE_APIURL;
export const BuildingsList = () => {
  const [showAddForm, setShowAddForm] = useState(false);
  const [newBuilding, setNewBuilding] = useState({ name: "", address: "" });
  const secureFetch = useSecureFetch();
  const user = userStore((s) => s.user);
  const navigate = useNavigate();
  const { data, isLoading } = useQuery<Building[]>({
    queryKey: ["buildings", user?.uuid],
    queryFn: async () => {
      const res = await secureFetch(`${API_URL}/buildings`);

      if (!res.ok) {
        throw new Error("Erreur lors de la récupération des bâtiments");
      }

      return (await res.json()) as Building[];
    },
    enabled: !!user?.uuid,
  });
  const queryClient = useQueryClient();

  const addBuilding = useMutation({
    mutationFn: async (formData: Omit<Building, "id" | "apartmentCount">) => {
      const response = await secureFetch(`${API_URL}/buildings`, {
        body: JSON.stringify(formData),
        method: "POST",
        headers: { "Content-Type": "application/json" },
      });
      if (!response.ok) throw new Error("Échec de l'ajout");
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["buildings", user?.uuid] });
    },
  });

  const handleAddBuilding = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await addBuilding.mutateAsync(newBuilding);
      setShowAddForm(false);
      setNewBuilding({ name: "", address: "" });
    } catch (err: unknown) {
      if (err instanceof Error) {
        console.error("Erreur lors de l'ajout du bâtiment :", err.message);
      }
      alert("Erreur lors de l'ajout");
    }
  };

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
      {showAddForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div
            className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm"
            onClick={() => setShowAddForm(false)}
          />

          <div className="relative bg-white w-full max-w-lg rounded-2xl shadow-2xl border border-slate-200 p-8 animate-in zoom-in-95 duration-200">
            <div className="mb-6">
              <h2 className="text-xl font-bold text-slate-800">
                Ajouter un bâtiment
              </h2>
              <p className="text-sm text-slate-500">
                Remplissez les informations pour créer un nouvel immeuble.
              </p>
            </div>

            <form onSubmit={handleAddBuilding} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Nom
                </label>
                <input
                  className="w-full border border-slate-300 p-3 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all"
                  placeholder="ex: Résidence Jupiter"
                  value={newBuilding.name}
                  onChange={(e) =>
                    setNewBuilding({ ...newBuilding, name: e.target.value })
                  }
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Adresse
                </label>
                <input
                  className="w-full border border-slate-300 p-3 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all"
                  placeholder="123 rue de la Paix, Paris"
                  value={newBuilding.address}
                  onChange={(e) =>
                    setNewBuilding({ ...newBuilding, address: e.target.value })
                  }
                  required
                />
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  type="button"
                  onClick={() => setShowAddForm(false)}
                  className="flex-1 px-4 py-3 text-slate-600 font-medium hover:bg-slate-100 rounded-xl transition-colors"
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  disabled={addBuilding.isPending}
                  className="flex-1 bg-indigo-600 text-white font-medium py-3 rounded-xl hover:bg-indigo-700 shadow-lg shadow-indigo-200 transition-all disabled:opacity-50"
                >
                  {addBuilding.isPending
                    ? "Enregistrement..."
                    : "Créer l'immeuble"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}{" "}
      {/* Liste des bâtiments */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {!isLoading &&
          data?.map((building: Building) => (
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

              <div className="mt-6 pt-4 border-t flex justify-between items-center text-sm font-medium">
                <span className="text-slate-400">Appartements</span>
                <span className="bg-slate-100 px-2 py-1 rounded text-slate-600">
                  {building.apartmentCount || 0}
                </span>
              </div>
            </div>
          ))}
      </div>
      {data && data.length === 0 && !showAddForm && (
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
