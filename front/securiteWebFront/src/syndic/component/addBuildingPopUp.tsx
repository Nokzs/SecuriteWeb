import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useSecureFetch } from "../../hooks/secureFetch";
import type { Building } from "../building/buildingList";
import { userStore } from "../../store/userStore";
import { useState } from "react";

type addBuildingPopUpProps = {
  setShowAddForm: React.Dispatch<React.SetStateAction<boolean>>;
};

const API_URL = import.meta.env.VITE_APIURL;
export const AddBuildingPopUp = ({ setShowAddForm }: addBuildingPopUpProps) => {
  const [newBuilding, setNewBuilding] = useState({ name: "", address: "" });
  const user = userStore((s) => s.user);
  const queryClient = useQueryClient();
  const secureFetch = useSecureFetch();
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
              {addBuilding.isPending ? "Enregistrement..." : "Créer l'immeuble"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
