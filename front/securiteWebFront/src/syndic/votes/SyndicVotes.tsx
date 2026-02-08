import { useQuery } from "@tanstack/react-query";
import { BarChart3, Lock, CheckCircle, XCircle, AlertCircle } from "lucide-react";
import { useSecureFetch } from "../../hooks/secureFetch";
const API_BASE = import.meta.env.VITE_APIURL;

interface VoteSummary {
  id: string;
  incidentTitle: string;
  status: string; // ONGOING, PASSED, REJECTED
  endDate: string;
  totalBuildingTantiemes: number;
  participationTantiemes: number;
  tantiemesFor: number;
  tantiemesAgainst: number;
  tantiemesAbstain: number;
}

export function SyndicVotes() {
  const secureFetch = useSecureFetch();

  // On utilise 'refetch' pour recharger la liste après une action
  const { data: votes, isLoading, refetch } = useQuery<VoteSummary[]>({
    queryKey: ["syndicVotes"],
    queryFn: async () => {
      const res = await secureFetch(`${API_BASE}/incidents/votes`);
      if (!res.ok) throw new Error("Erreur chargement votes");
      return await res.json();
    },
  });

  // Fonction pour fermer le vote manuellement
  const handleCloseVote = async (voteId: string) => {
    if (!window.confirm("Êtes-vous sûr de vouloir clôturer ce vote maintenant ?\n\nSi POUR > CONTRE, l'incident passera en 'Travaux'.\nSinon, il sera classé 'Clos'.")) {
      return;
    }

    try {
      const res = await secureFetch(`${API_BASE}/incidents/votes/${voteId}/close`, {
        method: "PUT",
      });

      if (res.ok) {
        refetch(); // Rafraîchir l'affichage immédiatement
      } else {
        alert("Erreur lors de la clôture du vote");
      }
    } catch (e) {
      console.error(e);
      alert("Erreur technique");
    }
  };

  if (isLoading) return <div className="p-10 text-center text-slate-500">Chargement des votes...</div>;

  return (
    <div className="max-w-6xl mx-auto p-6">
      <h1 className="text-2xl font-bold text-slate-800 mb-6 flex items-center gap-2">
        <BarChart3 className="text-indigo-600" />
        Suivi des Votes
      </h1>

      <div className="grid gap-6">
        {votes?.map((vote) => {
          // Calcul des pourcentages pour la barre visuelle
          const percentFor = Math.round((vote.tantiemesFor / vote.totalBuildingTantiemes) * 100) || 0;
          const percentAgainst = Math.round((vote.tantiemesAgainst / vote.totalBuildingTantiemes) * 100) || 0;
          
          const isOngoing = vote.status === "ONGOING";
          const isPassed = vote.status === "PASSED";

          return (
            <div key={vote.id} className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm relative overflow-hidden transition-all hover:shadow-md">
              
              {/* Bandeau de couleur vertical à gauche */}
              <div className={`absolute top-0 left-0 w-1.5 h-full ${
                 isOngoing ? 'bg-blue-500' : isPassed ? 'bg-green-500' : 'bg-red-500'
              }`}></div>

              <div className="flex justify-between items-start mb-4 pl-3">
                <div>
                  <h3 className="font-bold text-lg text-slate-800">{vote.incidentTitle}</h3>
                  <div className="flex items-center gap-3 mt-1">
                    {/* Badge de Statut */}
                    <span className={`text-xs font-bold px-3 py-1 rounded-full flex items-center gap-1 ${
                      isOngoing 
                        ? "bg-blue-100 text-blue-700" 
                        : isPassed 
                            ? "bg-green-100 text-green-700" 
                            : "bg-red-100 text-red-700"
                    }`}>
                      {isOngoing && <AlertCircle size={12}/>}
                      {isPassed && <CheckCircle size={12}/>}
                      {!isOngoing && !isPassed && <XCircle size={12}/>}
                      
                      {isOngoing ? "EN COURS" : isPassed ? "ADOPTÉ (Travaux)" : "REJETÉ (Clos)"}
                    </span>
                    
                    <span className="text-sm text-slate-500">
                       Fin : {new Date(vote.endDate).toLocaleDateString()}
                    </span>
                  </div>
                </div>

                {/* BOUTON D'ACTION (Visible seulement si vote en cours) */}
                {isOngoing && (
                  <button 
                    onClick={() => handleCloseVote(vote.id)}
                    className="flex items-center gap-2 px-4 py-2 bg-slate-800 text-white rounded-lg hover:bg-slate-700 transition-colors text-sm font-medium shadow-sm active:scale-95"
                    title="Arrêter le vote et calculer le résultat"
                  >
                    <Lock size={14} />
                    Clôturer le vote
                  </button>
                )}
              </div>

              {/* Barre de progression */}
              <div className="mb-1 flex justify-between text-xs font-semibold text-slate-500 uppercase tracking-wider">
                <span>Résultats ({vote.participationTantiemes} / {vote.totalBuildingTantiemes} tantièmes)</span>
              </div>
              <div className="h-6 w-full bg-slate-100 rounded-full overflow-hidden flex mb-4 border border-slate-100">
                <div style={{ width: `${percentFor}%` }} className="bg-green-500 h-full transition-all duration-500"></div>
                <div style={{ width: `${percentAgainst}%` }} className="bg-red-500 h-full transition-all duration-500"></div>
                {/* Le reste est gris (abstention/non votant) */}
              </div>

              {/* Légende Chiffrée */}
              <div className="flex gap-6 text-sm text-slate-700 bg-slate-50 p-3 rounded-lg">
                 <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full bg-green-500"></div>
                    <span className="font-bold">{vote.tantiemesFor}</span> <span className="text-slate-500">POUR</span>
                 </div>
                 <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full bg-red-500"></div>
                    <span className="font-bold">{vote.tantiemesAgainst}</span> <span className="text-slate-500">CONTRE</span>
                 </div>
                 <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full bg-slate-300"></div>
                    <span className="font-bold">{vote.tantiemesAbstain}</span> <span className="text-slate-500">ABSTENTION</span>
                 </div>
              </div>

            </div>
          );
        })}

        {votes?.length === 0 && (
            <div className="text-center py-12 bg-slate-50 rounded-xl border border-dashed border-slate-300">
                <BarChart3 className="mx-auto h-12 w-12 text-slate-300 mb-3" />
                <h3 className="text-lg font-medium text-slate-900">Aucun vote</h3>
                <p className="text-slate-500">Il n'y a pas de vote en cours ou passé pour vos immeubles.</p>
            </div>
        )}
      </div>
    </div>
  );
}