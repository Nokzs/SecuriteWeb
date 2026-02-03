import { useState } from "react";
import { AlertTriangle, Building2 } from "lucide-react";
import { useQuery, keepPreviousData } from "@tanstack/react-query";
import { useSecureFetch } from "../../hooks/secureFetch";
import { userStore, type UserStoreType } from "../../store/userStore";
import { IncidentCard } from "./incidentCard";
import { CreateVoteModal } from "./createVoteModal";
import type { Page } from "../../types/pagination";

// Interface alignée avec SyndicIncidentDto du Back-end
interface Incident {
  id: string;
  title: string;
  description: string;
  isUrgent: boolean;
  createdAt: string;
  ownerFirstName: string;
  ownerLastName: string;
  ownerPhone: string;
  buildingName: string;
  buildingAddress: string;
  apartmentNumber: string;
  photoCount: number;
  status: "PENDING" | "IGNORED" | "VOTED"; // Le back renvoie des MAJUSCULES
}

type SortOption = "urgent" | "recent" | "building" | "owner";
type FilterStatus = "ALL" | "PENDING" | "IGNORED" | "VOTED"; // On utilise des MAJUSCULES pour simplifier

const API_URL = import.meta.env.VITE_APIURL;

export function SyndicReclamations() {
  const secureFetch = useSecureFetch();
  const user = userStore((s: UserStoreType) => s.user);
  const get = userStore((s: UserStoreType) => s.get);
  const parsedUser = user ? get(user) : null;

  const [sortBy, setSortBy] = useState<SortOption>("urgent");
  // Valeur par défaut en majuscules pour correspondre au Back
  const [filterStatus, setFilterStatus] = useState<FilterStatus>("PENDING");
  const [selectedIncident, setSelectedIncident] = useState<Incident | null>(null);
  const [showVoteForm, setShowVoteForm] = useState(false);
  const [filter] = useState<{ limit: number; page: number }>({
    limit: 10,
    page: 0,
  });

  const { data, isLoading, refetch } = useQuery<Page<Incident>>({
    // On ajoute les filtres dans la clé de query pour recharger quand ça change
    queryKey: ["syndicIncidents", parsedUser?.uuid, filter, sortBy, filterStatus],
    queryFn: async () => {
      // Construction de l'URL avec gestion du filtre "ALL"
      const statusParam = filterStatus === "ALL" ? "" : `&status=${filterStatus}`;
      
      const res = await secureFetch(
        `${API_URL}/incidents?limit=${filter.limit}&page=${filter.page}&sortBy=${sortBy}${statusParam}`
      );

      if (!res.ok) {
        throw new Error("Erreur lors de la récupération des incidents");
      }

      return (await res.json()) as Page<Incident>;
    },
    enabled: !!parsedUser?.uuid,
    placeholderData: keepPreviousData,
  });

  const handleIgnoreIncident = async (incidentId: string) => {
    try {
      const response = await secureFetch(
        `${API_URL}/incidents/${incidentId}/ignore`,
        { method: "PATCH" }
      );

      if (!response.ok) {
        throw new Error("Erreur lors de l'ignorance de l'incident");
      }

      setSelectedIncident(null);
      refetch();
    } catch (err) {
      console.error(err);
    }
  };

  const handleVoteClick = (incident: Incident) => {
    setSelectedIncident(incident);
    setShowVoteForm(true);
  };

  const handleCreateVote = async (voteData: {
    amount: number;
    endDate: string;
  }) => {
    if (!selectedIncident) return;

    try {
      const response = await secureFetch(
        `${API_URL}/incidents/${selectedIncident.id}/vote`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            amount: voteData.amount,
            endDate: voteData.endDate,
            // CORRECTION : Pas besoin de buildingId ici, l'ID incident est dans l'URL
          }),
        }
      );

      if (!response.ok) {
        throw new Error("Erreur lors de la création du vote");
      }

      setShowVoteForm(false);
      setSelectedIncident(null);
      refetch();
    } catch (err) {
      console.error(err);
      throw err;
    }
  };

  const sortedIncidents = data?.content || [];

  if (isLoading) {
    return <span className="loading loading-spinner loading-lg"></span>;
  }

  return (
    <div className="max-w-7xl mx-auto p-6">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-slate-800 flex items-center gap-2">
          <AlertTriangle className="text-orange-600" size={28} />
          Réclamations et Incidents
        </h1>
        <p className="text-slate-500 mt-2">
          Gérez les incidents signalés par vos propriétaires
        </p>
      </div>

      {/* Info Banner */}
      <div className="bg-amber-50 border-l-4 border-amber-600 p-4 rounded-lg mb-8 flex gap-3">
        <AlertTriangle className="text-amber-600 flex-shrink-0" size={20} />
        <div>
          <h3 className="font-semibold text-amber-900">Gestion des incidents</h3>
          <p className="text-amber-800 text-sm mt-1">
            Vous pouvez ignorer un incident ou lancer un vote pour financer sa résolution auprès des propriétaires.
          </p>
        </div>
      </div>

      {/* Controls */}
      <div className="bg-white rounded-lg border border-slate-200 p-4 mb-6">
        <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center">
          {/* Status Filter */}
          <div className="flex-1">
            <label className="block text-sm font-semibold text-slate-700 mb-2">
              Statut
            </label>
            <div className="flex gap-2 flex-wrap">
              {[
                { value: "PENDING" as const, label: "En attente" },
                { value: "IGNORED" as const, label: "Ignorés" },
                { value: "VOTED" as const, label: "Votés" },
                { value: "ALL" as const, label: "Tous" },
              ].map((option) => (
                <button
                  key={option.value}
                  onClick={() => setFilterStatus(option.value)}
                  className={`px-4 py-2 rounded-lg font-medium transition ${
                    filterStatus === option.value
                      ? "bg-indigo-600 text-white"
                      : "bg-slate-100 text-slate-700 hover:bg-slate-200"
                  }`}
                >
                  {option.label}
                </button>
              ))}
            </div>
          </div>

          {/* Sort */}
          <div className="flex-1">
            <label className="block text-sm font-semibold text-slate-700 mb-2">
              Tri
            </label>
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value as SortOption)}
              className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:border-indigo-500"
            >
              <option value="urgent">Urgents en premier</option>
              <option value="recent">Plus récents en premier</option>
              <option value="building">Par immeuble</option>
              <option value="owner">Par propriétaire</option>
            </select>
          </div>
        </div>
      </div>

      {/* Incidents List */}
      {sortedIncidents.length === 0 ? (
        <div className="text-center py-20 bg-slate-50 rounded-2xl border-2 border-dashed border-slate-200">
          <Building2 className="mx-auto text-slate-300 mb-4" size={48} />
          <p className="text-slate-500 text-lg">
            {filterStatus === "PENDING"
              ? "Aucun incident en attente pour le moment"
              : "Aucun incident trouvé"}
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {sortedIncidents.map((incident) => (
            <IncidentCard
              key={incident.id}
              incident={incident}
              onSelect={() => setSelectedIncident(incident)}
              onIgnore={() => handleIgnoreIncident(incident.id)}
              onVote={() => handleVoteClick(incident)}
              isSelected={selectedIncident?.id === incident.id}
            />
          ))}
        </div>
      )}

      {/* Details Panel */}
      {selectedIncident && (
        <div className="fixed right-0 top-0 w-96 h-screen bg-white border-l border-slate-200 shadow-xl overflow-y-auto z-50 animate-in slide-in-from-right duration-300">
          <div className="p-6">
            <button
              onClick={() => setSelectedIncident(null)}
              className="text-gray-400 hover:text-gray-600 float-right p-1"
            >
              ✕
            </button>

            <h3 className="text-xl font-bold text-slate-800 mb-4 pr-6">
              {selectedIncident.title}
            </h3>

            {/* Status Badge */}
            <div className="mb-6 flex flex-wrap gap-2">
              <span
                className={`inline-block px-3 py-1 rounded-full text-sm font-semibold ${
                  selectedIncident.status === "PENDING"
                    ? "bg-yellow-100 text-yellow-800"
                    : selectedIncident.status === "IGNORED"
                      ? "bg-gray-100 text-gray-800"
                      : "bg-green-100 text-green-800"
                }`}
              >
                {selectedIncident.status === "PENDING"
                  ? "En attente"
                  : selectedIncident.status === "IGNORED"
                    ? "Ignoré"
                    : "Vote en cours"}
              </span>
              {selectedIncident.isUrgent && (
                <span className="inline-block px-3 py-1 rounded-full text-sm font-semibold bg-red-100 text-red-800 border border-red-200">
                  ⚠️ URGENT
                </span>
              )}
            </div>

            {/* Details */}
            <div className="space-y-6 mb-8">
              <div className="border-b border-slate-100 pb-4">
                <p className="text-xs text-slate-500 uppercase font-semibold mb-1">
                  Propriétaire
                </p>
                <p className="text-slate-800 font-medium text-lg">
                  {selectedIncident.ownerFirstName} {selectedIncident.ownerLastName}
                </p>
                <p className="text-sm text-slate-600 flex items-center gap-1 mt-1">
                  📞 {selectedIncident.ownerPhone}
                </p>
              </div>

              <div className="border-b border-slate-100 pb-4">
                <p className="text-xs text-slate-500 uppercase font-semibold mb-1">
                  Localisation
                </p>
                <p className="text-slate-800 font-medium">{selectedIncident.buildingName}</p>
                <p className="text-sm text-slate-600">{selectedIncident.buildingAddress}</p>
                <p className="text-sm font-medium text-indigo-600 mt-1">
                  Appartement Nº {selectedIncident.apartmentNumber}
                </p>
              </div>

              <div className="border-b border-slate-100 pb-4">
                <p className="text-xs text-slate-500 uppercase font-semibold mb-1">
                  Date de signalement
                </p>
                <p className="text-slate-800">
                  {new Date(selectedIncident.createdAt).toLocaleDateString("fr-FR", {
                    day: "numeric",
                    month: "long",
                    year: "numeric",
                    hour: "2-digit",
                    minute: "2-digit"
                  })}
                </p>
              </div>

              <div>
                <p className="text-xs text-slate-500 uppercase font-semibold mb-2">
                  Description
                </p>
                <div className="bg-slate-50 p-4 rounded-lg border border-slate-100">
                    <p className="text-slate-700 text-sm leading-relaxed whitespace-pre-wrap">
                    {selectedIncident.description}
                    </p>
                </div>
              </div>

              {selectedIncident.photoCount > 0 && (
                <div className="bg-blue-50 p-3 rounded-lg border border-blue-100 text-blue-700 flex items-center gap-2">
                  <span>📷</span>
                  <span className="text-sm font-medium">
                     {selectedIncident.photoCount} photo(s) jointe(s)
                  </span>
                </div>
              )}
            </div>

            {/* Actions */}
            {selectedIncident.status === "PENDING" && (
              <div className="flex flex-col gap-3 mt-auto">
                <button
                  onClick={() => setShowVoteForm(true)}
                  className="w-full px-4 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl transition font-semibold shadow-lg shadow-indigo-200"
                >
                  Lancer un vote
                </button>
                <button
                  onClick={() => handleIgnoreIncident(selectedIncident.id)}
                  className="w-full px-4 py-3 border border-slate-200 text-slate-600 rounded-xl hover:bg-slate-50 transition font-medium"
                >
                  Ignorer cet incident
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Vote Form Modal */}
      {showVoteForm && selectedIncident && (
        <CreateVoteModal
          incidentTitle={selectedIncident.title}
          buildingName={selectedIncident.buildingName}
          onClose={() => setShowVoteForm(false)}
          onSubmit={handleCreateVote}
        />
      )}
    </div>
  );
}