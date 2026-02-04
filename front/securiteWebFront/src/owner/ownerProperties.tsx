import { useState } from "react";
import { AlertCircle, Home } from "lucide-react";
import { useQuery, keepPreviousData } from "@tanstack/react-query";
import { useSecureFetch } from "../hooks/secureFetch";
import { useUploadFile } from "../hooks/useUploadFile";
import { userStore, type UserStoreType } from "../store/userStore";
import { OwnerPropertyCard } from "./component/ownerPropertyCard";
import { IncidentFormModal } from "./component/incidentFormModal";
import type { Page } from "../types/pagination";
import { API_BASE } from "../config/urls";
interface IncidentResponse {
  id: string;
  photoUrls?: string[];
}

interface Property {
  id: string;
  numero: string;
  photoFilename?: string | null;

  buildingName: string;
  buildingAddress: string;
}

export function OwnerProperties() {
  const secureFetch = useSecureFetch();
  const uploadFile = useUploadFile();
  const user = userStore((s: UserStoreType) => s.user);
  const get = userStore((s: UserStoreType) => s.get);
  const parsedUser = user ? get(user) : null;

  const [showIncidentForm, setShowIncidentForm] = useState(false);
  const [selectedProperty, setSelectedProperty] = useState<Property | null>(
    null,
  );

  const [filter] = useState<{ limit: number; page: number }>({
    limit: 6,
    page: 0,
  });

  const { data, isLoading } = useQuery<Page<Property>>({
    queryKey: ["ownerProperties", parsedUser?.uuid, filter],
    queryFn: async () => {
      const res = await secureFetch(
        `${API_BASE}/apartment?limit=${filter.limit}&page=${filter.page}`,
      );
      if (!res.ok) throw new Error("Erreur fetch properties");
      return (await res.json()) as Page<Property>;
    },
    enabled: !!parsedUser?.uuid,
    placeholderData: keepPreviousData,
  });

  // --- 2. Ouverture du formulaire ---
  const handleIncidentClick = (property: Property) => {
    setSelectedProperty(property);
    setShowIncidentForm(true);
  };

  // --- 3. Soumission du formulaire ---
  const handleIncidentSubmit = async (formData: {
    title: string;
    description: string;
    isUrgent: boolean;
    photos: File[];
  }) => {
    if (!selectedProperty) return;

    try {
      // A. Création de l'incident (JSON)
      const payload = {
        title: formData.title,
        description: formData.description,
        isUrgent: formData.isUrgent,
        apartmentId: selectedProperty.id,
        photoFilenames: formData.photos.map((f) => f.name),
      };

      const response = await secureFetch(`${API_BASE}/incidents`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!response.ok) throw new Error("Erreur création incident");

      const createdIncident: IncidentResponse = await response.json();

      // B. Upload des photos (MinIO)
      if (formData.photos.length > 0 && createdIncident.photoUrls?.length) {
        const uploadPromises = formData.photos.map((file, index) => {
          const signedUrl = createdIncident.photoUrls![index];
          return signedUrl ? uploadFile(file, signedUrl) : Promise.resolve();
        });
        await Promise.all(uploadPromises);
      }

      // C. Reset
      setShowIncidentForm(false);
      setSelectedProperty(null);
    } catch (err) {
      console.error(err);
      throw err instanceof Error ? err : new Error("Une erreur est survenue");
    }
  };

  if (isLoading)
    return <span className="loading loading-spinner loading-lg"></span>;

  return (
    <div className="max-w-6xl mx-auto">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-slate-800">Mes logements</h1>
        <p className="text-slate-500">
          Consultez la liste de vos biens et signalez des incidents si
          nécessaire.
        </p>
      </div>

      {/* Banner */}
      <div className="bg-blue-50 border-l-4 border-blue-600 p-4 rounded-lg mb-8 flex gap-3">
        <AlertCircle className="text-blue-600 flex-shrink-0" size={20} />
        <div>
          <h3 className="font-semibold text-blue-900">Besoin d'aide ?</h3>
          <p className="text-blue-800 text-sm mt-1">
            Cliquez sur le bouton "Signaler un incident" sur une carte pour
            ouvrir un ticket.
          </p>
        </div>
      </div>

      {/* Grid */}
      {data && data.content.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-2xl border-2 border-dashed border-slate-200">
          <Home className="mx-auto text-slate-300 mb-4" size={48} />
          <p className="text-slate-500">Aucun logement enregistré.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {data?.content.map((property) => (
            <OwnerPropertyCard
              key={property.id}
              property={property}
              onIncidentClick={() => handleIncidentClick(property)}
            />
          ))}
        </div>
      )}

      {/* Modal */}
      {showIncidentForm && selectedProperty && (
        <IncidentFormModal
          propertyName={selectedProperty.numero}
          onClose={() => {
            setShowIncidentForm(false);
            setSelectedProperty(null);
          }}
          onSubmit={handleIncidentSubmit}
        />
      )}
    </div>
  );
}
