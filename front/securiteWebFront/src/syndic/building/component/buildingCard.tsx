import { useNavigate } from "react-router";
import type { Building } from "../buildingList";
import { Building2, ChevronRight, MapPin } from "lucide-react";

type BuildingCardProps = {
  building: Building;
};

export const BuildingCard = ({ building }: BuildingCardProps) => {
  const navigate = useNavigate();
  // Calcul du pourcentage de tantièmes
  const total = building.totalTantieme || 0;
  const current = building.currentTantieme || 0;
  const percentage =
    total > 0 ? Math.min(Math.round((current / total) * 100), 100) : 0;

  return (
    <div
      key={building.id}
      onClick={() => navigate(`/syndic/building/${building.id}/apartments`)}
      className="group cursor-pointer bg-white border border-slate-200 p-5 rounded-xl hover:border-indigo-400 hover:shadow-md transition-all relative overflow-hidden"
    >
      <div className="flex items-start justify-between items-center">
        <div className="p-3 bg-indigo-50 text-indigo-600 rounded-lg group-hover:bg-indigo-600 group-hover:text-white transition-colors">
          {building.photoFilename ? (
            <img src={building.photoFilename} className="h-26 mx-auto" />
          ) : (
            <Building2 size={24} />
          )}
        </div>
        <ChevronRight className="text-slate-300 group-hover:text-indigo-500 transition-colors" />
      </div>

      <h3 className="mt-4 font-bold text-lg text-slate-800">{building.name}</h3>

      <div className="flex items-center gap-1 text-slate-500 text-sm mt-1">
        <MapPin size={14} />

        <p>{building.adresse}</p>
      </div>

      {/* Section Tantièmes et Barre de progression */}
      <div className="mt-6">
        <div className="flex justify-between items-end mb-2">
          <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">
            Tantièmes
          </span>
          <span className="text-sm font-bold text-slate-700">
            {current.toLocaleString()} / {total.toLocaleString()}
          </span>
        </div>

        <div className="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
          <div
            className="bg-indigo-500 h-full rounded-full transition-all duration-500 ease-out group-hover:bg-indigo-600"
            style={{ width: `${percentage}%` }}
          />
        </div>

        <p className="text-[10px] text-slate-400 mt-1 text-right">
          {percentage}% répartis
        </p>
      </div>
    </div>
  );
};
