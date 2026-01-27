import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useParams } from "react-router";
import { userStore } from "../../../store/userStore";
import { useSecureFetch } from "../../../hooks/secureFetch";
import { useUploadFile } from "../../../hooks/useUploadFile";
import { OwnerSearchByEmail } from "./OwnerSearchByEmail";
import type { Apartment } from "./apartmentsList";

type AddApartmentPopUpProps = {
  setShowAddForm: React.Dispatch<React.SetStateAction<boolean>>;
  building?: { totalTantieme?: number | null; currentTantieme?: number | null };
};

const API_URL = import.meta.env.VITE_APIURL;

export const AddApartmentPopUp = ({
  setShowAddForm,
  building,
}: AddApartmentPopUpProps) => {
  const { buildingId } = useParams<{ buildingId: string }>();
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<string | null>(null);
  const [newApartment, setNewApartment] = useState<
    Omit<Apartment, "buildingUuid" | "id" | "ownerId" | "photoFilename"> & {
      ownerEmail?: string;
    }
  >({
    numero: "",
    etage: undefined,
    nombrePieces: undefined,
    tantiemes: 0,
    surface: undefined,
    ownerEmail: "",
  });

  const user = userStore(
    (state: { user: { uuid?: string } | null }) => state.user,
  );
  const queryClient = useQueryClient();
  const secureFetch = useSecureFetch();
  const uploadFile = useUploadFile();

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      setSelectedFile(file);
      setPreview(URL.createObjectURL(file));
    }
  };
  const ownerEmailChange = (email: string) => {
    setNewApartment({ ...newApartment, ownerEmail: email });
  };
  const totalTantieme = building?.totalTantieme ?? 0;
  const currentTantieme = building?.currentTantieme ?? 0;
  const remainingTantiemes = Math.max(totalTantieme - currentTantieme, 0);
  const exceedsQuota =
    totalTantieme > 0 && newApartment.tantiemes > remainingTantiemes;

  const addApartment = useMutation({
    mutationFn: async (
      formData: Omit<
        Apartment,
        "id" | "ownerId" | "buildingUuid" | "photoFilename"
      > & {
        photoFilename: string | null;
      },
    ) => {
      const data = { ...formData, buildingId: buildingId };
      const response = await secureFetch(`${API_URL}/apartment`, {
        body: JSON.stringify(data),
        method: "POST",
        headers: { "Content-Type": "application/json" },
      });

      if (!response.ok) throw new Error("Échec de l'ajout");
      return response.json() as Promise<Apartment>;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["apartments", user?.uuid, buildingId],
      });
    },
  });

  const handleAddApartment = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!buildingId) {
      alert("Aucun immeuble sélectionné");
      return;
    }

    if (exceedsQuota) {
      alert(
        `Impossible de créer l'appartement : tantièmes demandés (${newApartment.tantiemes}) > tantièmes restants (${remainingTantiemes}).`,
      );
      return;
    }

    try {
      const apartment: Apartment = await addApartment.mutateAsync({
        ...newApartment,
        photoFilename: selectedFile ? selectedFile.name : null,
      });

      if (selectedFile && apartment.photoFilename) {
        await uploadFile(selectedFile, apartment.photoFilename);
      }

      setShowAddForm(false);
      setSelectedFile(null);
      setPreview(null);
    } catch (err: unknown) {
      if (err instanceof Error) {
        console.error("Erreur lors de l'ajout de l'appartement :", err.message);
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
            Ajouter un appartement
          </h2>
          <p className="text-sm text-slate-500">
            Remplissez les informations et ajoutez une photo.
          </p>
        </div>

        <OwnerSearchByEmail setOwnerEmail={ownerEmailChange} />
        <form onSubmit={handleAddApartment} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Numéro
            </label>
            <input
              className="w-full border border-slate-300 text-black p-3 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all"
              placeholder='ex: "A101"'
              value={newApartment.numero}
              onChange={(e) =>
                setNewApartment({ ...newApartment, numero: e.target.value })
              }
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                Étage
              </label>
              <input
                type="number"
                className="w-full border border-slate-300 text-black p-3 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all"
                value={newApartment.etage}
                onChange={(e) =>
                  setNewApartment({
                    ...newApartment,
                    etage: Number(e.target.value),
                  })
                }
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                Surface (m²)
              </label>
              <input
                type="number"
                className="w-full border border-slate-300 text-black p-3 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all"
                value={newApartment.surface}
                onChange={(e) =>
                  setNewApartment({
                    ...newApartment,
                    surface: Number(e.target.value),
                  })
                }
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                Nombre de pièces
              </label>
              <input
                type="number"
                min={1}
                className="w-full border border-slate-300 text-black p-3 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all"
                value={newApartment.nombrePieces}
                onChange={(e) =>
                  setNewApartment({
                    ...newApartment,
                    nombrePieces: Number(e.target.value),
                  })
                }
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                E-mail du propriétaire
              </label>
              <input
                type="text"
                className="w-full border border-slate-300 text-black p-3 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all"
                value={newApartment.ownerEmail}
                disabled
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Tantièmes
            </label>
            <input
              type="number"
              min={0}
              className={`w-full border text-black p-3 rounded-xl outline-none transition-all focus:ring-2 focus:ring-indigo-500 ${
                exceedsQuota ? "border-red-400" : "border-slate-300"
              }`}
              value={newApartment.tantiemes}
              onChange={(e) =>
                setNewApartment({
                  ...newApartment,
                  tantiemes: Number(e.target.value),
                })
              }
              required
            />
            {!!building && totalTantieme > 0 && (
              <p className="text-xs mt-1 text-slate-500">
                Restant:{" "}
                <span className="font-semibold">{remainingTantiemes}</span>
                {exceedsQuota && (
                  <span className="text-red-600 font-medium">
                    {" "}
                    — quota dépassé
                  </span>
                )}
              </p>
            )}
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Photo de l'appartement
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
              disabled={addApartment.isPending || exceedsQuota}
              className="flex-1 bg-indigo-600 text-white font-medium py-3 rounded-xl hover:bg-indigo-700 shadow-lg shadow-indigo-200 transition-all disabled:opacity-50"
            >
              {addApartment.isPending ? "Envoi..." : "Créer l'appartement"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
