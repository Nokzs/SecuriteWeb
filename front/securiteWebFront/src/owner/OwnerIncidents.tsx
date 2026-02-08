import { useQuery } from "@tanstack/react-query";
import { Building2, Clock, CheckCircle, XCircle, Hammer, Lock } from "lucide-react";
import { useSecureFetch } from "../hooks/secureFetch";
import { VoteCard } from "./component/VoteCard"; 

const API_BASE = import.meta.env.VITE_APIURL;

// 1. Mise à jour de l'interface
interface OwnerIncident {
  id: string;
  title: string;
  status: "PENDING" | "IGNORED" | "VOTED" | "IN_PROGRESS" | "RESOLVED";
  createdAt: string; 
  voteId?: string;
  voteAmount?: number;
  voteEndDate?: string;  
  hasVoted: boolean; 
}

export function OwnerIncidents() {
  const secureFetch = useSecureFetch();

  const { data: incidents, isLoading, refetch } = useQuery<OwnerIncident[]>({
    queryKey: ["ownerIncidents"],
    queryFn: async () => {
      const res = await secureFetch(`${API_BASE}/incidents/me`);
      if (!res.ok) throw new Error("Impossible de récupérer les incidents");
      return await res.json();
    },
  });

  if (isLoading) return <span className="loading loading-spinner loading-lg text-indigo-600 block mx-auto mt-10"></span>;

  return (
    <div className="max-w-5xl mx-auto p-4">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-slate-800 flex items-center gap-3">
          <Building2 className="text-indigo-600" size={28} />
          Vie de l'Immeuble & Votes
        </h1>
        <p className="text-slate-500 mt-1">
            Suivez tout ce qui se passe dans la copropriété et votez pour les décisions importantes.
        </p>
      </div>

      <div className="space-y-6">
        {incidents?.map((incident) => (
          <div key={incident.id} className="relative transition-all duration-300">
            
            {/* LOGIQUE D'AFFICHAGE DU VOTE */}
            {(incident.status === "VOTED" || incident.status === "PENDING") && incident.voteId ? (
              
              <div className="mb-6 animate-in fade-in slide-in-from-bottom-2 duration-500">
                
                {/* CAS A : L'utilisateur a DEJA voté -> On affiche un résumé bloqué */}
                {incident.hasVoted ? (
                   <div className="bg-green-50 border border-green-200 rounded-xl p-6 flex flex-col items-center justify-center text-center shadow-sm">
                      <div className="bg-green-100 p-3 rounded-full mb-3">
                        <CheckCircle className="text-green-600" size={32} />
                      </div>
                      <h3 className="text-lg font-bold text-green-800">Votre vote a été enregistré</h3>
                      <p className="text-green-700 mb-2">"{incident.title}"</p>
                      <p className="text-sm text-green-600 flex items-center gap-2">
                        <Lock size={14} /> Vous ne pouvez plus modifier votre choix.
                      </p>
                   </div>
                ) : (
                   /* CAS B : L'utilisateur n'a PAS voté -> On affiche les boutons */
                   <>
                      <div className="flex items-center gap-2 mb-2 text-indigo-700 font-semibold animate-pulse">
                        <Clock size={18} />
                        Action requise : Vote en cours pour cet incident
                      </div>
                      <VoteCard 
                        voteId={incident.voteId}
                        incidentTitle={incident.title}
                        amount={incident.voteAmount || 0}
                        endDate={incident.voteEndDate || ""}
                        onVoteSuccess={() => {
                          refetch(); // Recharger la liste pour passer hasVoted à true !
                        }}
                      />
                   </>
                )}
              </div>

            ) : (
              /* AFFICHAGE STANDARD (Pas de vote en cours) */
              <div className={`p-5 rounded-xl border flex flex-col md:flex-row justify-between md:items-center gap-4 ${
                  incident.status === "IGNORED" 
                    ? "bg-slate-50 border-slate-200 opacity-60" 
                    : "bg-white border-slate-200 shadow-sm"
                }`}>
                
                <div>
                  <h3 className={`font-semibold text-lg ${incident.status === "IGNORED" ? "text-slate-500 line-through" : "text-slate-800"}`}>
                    {incident.title}
                  </h3>
                  <p className="text-sm text-slate-500 flex items-center gap-2 mt-1">
                    <span>📅 {new Date(incident.createdAt).toLocaleDateString("fr-FR")}</span>
                  </p>
                </div>
                
                <div className="flex items-center gap-2">
                  {incident.status === "PENDING" && (
                    <span className="bg-yellow-100 text-yellow-800 px-3 py-1 rounded-full text-xs font-bold flex items-center gap-1">
                      <Clock size={12}/> EN ATTENTE
                    </span>
                  )}
                  {incident.status === "IGNORED" && (
                    <span className="bg-gray-200 text-gray-600 px-3 py-1 rounded-full text-xs font-bold flex items-center gap-1">
                      <XCircle size={12}/> REFUSÉ
                    </span>
                  )}
                  {incident.status === "IN_PROGRESS" && (
                    <span className="bg-purple-100 text-purple-800 px-3 py-1 rounded-full text-xs font-bold flex items-center gap-1 border border-purple-200">
                      <Hammer size={12}/> TRAVAUX VALIDÉS
                    </span>
                  )}
                  {incident.status === "RESOLVED" && (
                    <span className="bg-green-100 text-green-800 px-3 py-1 rounded-full text-xs font-bold flex items-center gap-1 border border-green-200">
                      <CheckCircle size={12}/> TERMINÉ
                    </span>
                  )}
                </div>
              </div>
            )}
          </div>
        ))}
        
        {incidents?.length === 0 && (
            <div className="text-center py-12 text-slate-500">Aucun incident.</div>
        )}
      </div>
    </div>
  );
}