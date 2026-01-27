import { Home } from "lucide-react";
import type { Apartment } from "./apartmentsList";

type ApartmentCardProps = {
  apartment: Apartment;
};
export const ApartmentCard = ({ apartment }: ApartmentCardProps) => {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-100 p-5 hover:shadow-md transition-all">
      <div className="flex items-start gap-4">
        <div className="shrink-0">
          <div className="p-3 bg-indigo-50 text-indigo-600 rounded-lg">
            {apartment.photoFilename ? (
              <img
                src={apartment.photoFilename}
                alt={apartment.numero}
                className="h-12 w-12 object-cover rounded-md"
              />
            ) : (
              <Home size={24} />
            )}
          </div>
        </div>

        <div className="flex-1">
          <h3 className="text-lg font-semibold text-slate-800">
            {apartment.numero}
          </h3>
          <p className="text-sm text-slate-500 mt-1">
            Étage {apartment.etage} · {apartment.nombrePieces} pièce(s)
          </p>
          <p className="text-sm text-slate-500 mt-1">
            Tantièmes: {apartment.tantiemes}
          </p>
          <p className="text-sm text-slate-500 mt-1">
            Tantièmes:{" "}
            {apartment.ownerId ? apartment.ownerId : "Sans propriétaire"}
          </p>
        </div>
      </div>
    </div>
  );
};
