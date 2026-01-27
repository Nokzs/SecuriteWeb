import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useSecureFetch } from "../../hooks/secureFetch";
import type { Building } from "../building/buildingList";
import { userStore } from "../../store/userStore";
import { useState } from "react";
import { useUploadFile } from "../../hooks/useUploadFile";

type addBuildingPopUpProps = {
  setShowAddForm: React.Dispatch<React.SetStateAction<boolean>>;
};

const API_URL = import.meta.env.VITE_APIURL;
export const AddBuildingPopUp = ({ setShowAddForm }: addBuildingPopUpProps) => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<string | null>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      setSelectedFile(file);
      setPreview(URL.createObjectURL(file));
    }
  };
  const [newBuilding, setNewBuilding] = useState({ name: "", adresse: "" });
  const user = userStore((s) => s.user);
  const get = userStore((s) => s.get);
  const parsedUser = user ? get(user) : null;
  const queryClient = useQueryClient();
  const secureFetch = useSecureFetch();
  const uploadFile = useUploadFile();
  const addBuilding = useMutation({
    mutationFn: async (
      formData: Omit<
        Building,
        "id" | "syndicId" | "totalTantieme" | "currentTantieme"
      >,
    ) => {
      const response = await secureFetch(`${API_URL}/building`, {
        body: JSON.stringify(formData),
        method: "POST",
        headers: { "Content-Type": "application/json" },
      });
      if (!response.ok) throw new Error("Échec de l'ajout");
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["buildings", parsedUser?.uuid] });
    },
  });
  const handleAddBuilding = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const data = {
        ...newBuilding,
        photoFilename: selectedFile ? selectedFile.name : null,
      };
      const building: Building = await addBuilding.mutateAsync(data);

      if (selectedFile) {
        const link = building.photoFilename;
        if (!link) return;
        uploadFile(selectedFile, link);
      }
      setShowAddForm(false);
      setSelectedFile(null);
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

      <div className="relative bg-white  w-full max-w-lg rounded-2xl shadow-2xl border border-slate-200 p-8 animate-in zoom-in-95 duration-200">
        <div className="mb-6">
          <h2 className="text-xl font-bold text-slate-800">
            Ajouter un bâtiment
          </h2>
          <p className="text-sm text-slate-500">
            Remplissez les informations et ajoutez une photo.
          </p>
        </div>

        <form onSubmit={handleAddBuilding} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Nom
            </label>
            <input
              className="w-full border border-slate-300 text-black p-3 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all"
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
              className="w-full border border-slate-300 text-black p-3 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all"
              placeholder="123 rue de la Paix, Paris"
              value={newBuilding.adresse}
              onChange={(e) =>
                setNewBuilding({ ...newBuilding, adresse: e.target.value })
              }
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Photo de l'immeuble
            </label>
            <div className="relative group border-2 border-dashed border-slate-200 rounded-xl p-4 hover:border-indigo-400 transition-colors bg-slate-50">
              <input
                type="file"
                accept="image/*"
                onChange={handleFileChange}
                className="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10"
              />

              <div className="flex flex-col items-center justify-center space-y-2">
                {preview ? (
                  <div className="relative w-full h-32">
                    <img
                      src={preview}
                      alt="Preview"
                      className="w-full h-full object-cover rounded-lg"
                    />
                    <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center rounded-lg text-white text-xs">
                      Changer la photo
                    </div>
                  </div>
                ) : (
                  <>
                    <div className="p-3 bg-white rounded-full shadow-sm text-slate-400 group-hover:text-indigo-500 transition-colors">
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="h-6 w-6"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                        />
                      </svg>
                    </div>
                    <p className="text-xs text-slate-500">
                      Cliquez ou glissez une image ici
                    </p>
                  </>
                )}
              </div>
            </div>
          </div>
          {/* ----------------------------- */}

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
              {addBuilding.isPending ? "Envoi..." : "Créer l'immeuble"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
