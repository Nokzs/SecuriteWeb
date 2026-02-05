import { AlertTriangle, Clock, MapPin, User, ChevronRight } from "lucide-react";

interface Incident {
  id: string;
  title: string;
  description: string;
  isUrgent: boolean;
  createdAt: string;
  ownerFirstName: string;
  ownerLastName: string;
  buildingName: string;
  apartmentNumber: string;
  status: "PENDING" | "IGNORED" | "VOTED";
}

interface IncidentCardProps {
  incident: Incident;
  onSelect: () => void;
  onIgnore: () => void;
  onVote: () => void;
  isSelected: boolean;
}

export function IncidentCard({
  incident,
  onSelect,
  onIgnore,
  onVote,
  isSelected,
}: IncidentCardProps) {
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("fr-FR", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  return (
    <div
      onClick={onSelect}
      className={`p-5 rounded-lg border-2 cursor-pointer transition-all ${
        isSelected
          ? "border-indigo-600 bg-indigo-50"
          : "border-slate-200 bg-white hover:border-slate-300"
      }`}
    >
      <div className="flex items-start justify-between gap-4">
        {/* Left Content */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-2">
            <h3 className="text-lg font-bold text-slate-800 truncate">
              {incident.title}
            </h3>
            {incident.isUrgent && (
              <span className="flex-shrink-0 inline-flex items-center gap-1 px-2 py-1 rounded-full bg-red-100 text-red-800 text-xs font-bold">
                <AlertTriangle size={12} />
                URGENT
              </span>
            )}
          </div>

          <div className="space-y-2 text-sm">
            <div className="flex items-center gap-2 text-slate-600">
              <MapPin size={16} className="flex-shrink-0" />
              <span className="truncate">
                {incident.buildingName} - Apt. {incident.apartmentNumber}
              </span>
            </div>

            <div className="flex items-center gap-2 text-slate-600">
              <User size={16} className="flex-shrink-0" />
              <span className="truncate">
                {incident.ownerFirstName} {incident.ownerLastName}
              </span>
            </div>

            <div className="flex items-center gap-2 text-slate-500">
              <Clock size={16} className="flex-shrink-0" />
              <span>{formatDate(incident.createdAt)}</span>
            </div>
          </div>

          <p className="text-slate-600 text-sm mt-3 line-clamp-2">
            {incident.description}
          </p>
        </div>

        {/* Status & Actions */}
        <div className="flex flex-col items-end gap-2 flex-shrink-0">
          {/* Status Badge */}
          <span
            className={`px-3 py-1 rounded-full text-xs font-semibold ${
              incident.status === "PENDING"
                ? "bg-yellow-100 text-yellow-800"
                : incident.status === "IGNORED"
                  ? "bg-gray-100 text-gray-800"
                  : "bg-green-100 text-green-800"
            }`}
          >
            {incident.status === "PENDING"
              ? "En attente"
              : incident.status === "IGNORED"
                ? "Ignoré"
                : "Voté"}
          </span>

          <ChevronRight className="text-slate-300" size={20} />
        </div>
      </div>

      {/* Actions - Show on hover or selection */}
      {isSelected && incident.status === "PENDING" && (
        <div className="mt-4 pt-4 border-t border-indigo-200 flex gap-2">
          <button
            onClick={(e) => {
              e.stopPropagation();
              onIgnore();
            }}
            className="flex-1 px-3 py-2 border border-slate-300 text-slate-700 rounded-lg hover:bg-slate-50 transition font-semibold text-sm"
          >
            Ignorer
          </button>
          <button
            onClick={(e) => {
              e.stopPropagation();
              onVote();
            }}
            className="flex-1 px-3 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition font-semibold text-sm"
          >
            Lancer un vote
          </button>
        </div>
      )}
    </div>
  );
}
