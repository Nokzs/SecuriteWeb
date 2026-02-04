import { Building2, ChevronRight, MapPin, AlertCircle } from "lucide-react";

// Interface alignée STRICTEMENT avec le Backend (pas de zipcode/city)
interface Property {
  id: string;
  numero: string;
  photoFilename?: string | null;
  
  // Champs de l'immeuble existants en base
  buildingName: string;
  buildingAddress: string;
}

interface OwnerPropertyCardProps {
  property: Property;
  onIncidentClick: () => void;
}

const API_URL = import.meta.env.VITE_APIURL;

export function OwnerPropertyCard({ property, onIncidentClick }: OwnerPropertyCardProps) {
  
  // Gestion sécurisée de l'image
  const imageUrl = property.photoFilename
    ? (property.photoFilename.startsWith("http")
        ? property.photoFilename
        : `${API_URL}/files/${property.photoFilename}`)
    : null;

  return (
    <div className="group bg-white border border-slate-200 p-5 rounded-xl hover:border-indigo-400 hover:shadow-md transition-all overflow-hidden flex flex-col h-full">
      
      {/* Header : Photo & Icône */}
      <div className="flex items-start justify-between mb-4">
        <div className="h-16 w-16 p-1 bg-indigo-50 text-indigo-600 rounded-lg group-hover:bg-indigo-600 group-hover:text-white transition-colors overflow-hidden flex items-center justify-center">
          {imageUrl ? (
            <img 
              src={imageUrl} 
              alt={`Appartement ${property.numero}`} 
              className="w-full h-full object-cover rounded-md" 
            />
          ) : (
            <Building2 size={28} />
          )}
        </div>
        <ChevronRight className="text-slate-300 group-hover:text-indigo-500 transition-colors" />
      </div>

      {/* Property Info */}
      <div className="flex-grow">
        <h3 className="font-bold text-lg text-slate-800 leading-tight">
          {property.buildingName || "Résidence"}
        </h3>
        <p className="text-indigo-600 font-medium text-sm mt-1">
          Appartement {property.numero}
        </p>

        <div className="flex items-start gap-2 text-slate-500 text-sm mt-4 mb-6">
          <MapPin size={16} className="mt-0.5 flex-shrink-0" />
          <p className="line-clamp-2">
            {/* On affiche uniquement l'adresse globale renvoyée par le back */}
            {property.buildingAddress || "Adresse non renseignée"}
          </p>
        </div>
      </div>

      {/* Action Button */}
      <button
        onClick={onIncidentClick}
        className="w-full flex items-center justify-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-2.5 px-4 rounded-lg transition-colors mt-auto"
      >
        <AlertCircle size={18} />
        Signaler un incident
      </button>
    </div>
  );
}