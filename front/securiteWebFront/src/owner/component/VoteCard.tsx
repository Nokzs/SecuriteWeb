import { useState } from "react";
import { Check, X, Minus, Calendar, Euro } from "lucide-react";
import { useSecureFetch } from "../../hooks/secureFetch"; // Adapte le chemin selon ta structure
const API_BASE = import.meta.env.VITE_APIURL;

export type VoteChoice = "FOR" | "AGAINST" | "ABSTAIN";

interface VoteProps {
  voteId: string;
  incidentTitle: string;
  amount: number;
  endDate: string;
  onVoteSuccess: () => void;
}

export function VoteCard({ voteId, incidentTitle, amount, endDate, onVoteSuccess }: VoteProps) {
  const secureFetch = useSecureFetch();
  const [isLoading, setIsLoading] = useState(false);
  const [hasVoted, setHasVoted] = useState(false); // Pour l'état local immédiat

  const handleVote = async (choice: VoteChoice) => {
    if (!confirm(`Confirmez-vous votre vote : ${choice === 'FOR' ? 'POUR' : choice === 'AGAINST' ? 'CONTRE' : 'ABSTENTION'} ?`)) return;

    try {
      setIsLoading(true);

      const response = await secureFetch(`${API_BASE}/incidents/votes/${voteId}/cast`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ choice }),
      });

      if (!response.ok) {
        const error = await response.json();
        alert(error.message || "Erreur lors du vote");
        return;
      }

      setHasVoted(true);
      if (onVoteSuccess) onVoteSuccess();
      
    } catch (err) {
      console.error(err);
      alert("Erreur de connexion");
    } finally {
      setIsLoading(false);
    }
  };

  if (hasVoted) {
    return (
      <div className="bg-green-50 border border-green-200 p-6 rounded-xl flex items-center justify-center text-green-700 font-medium">
        <Check size={20} className="mr-2" />
        A voté ! Merci de votre participation.
      </div>
    );
  }

  return (
    <div className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden">
      {/* Header : Titre et Montant */}
      <div className="p-5 border-b border-slate-100 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h3 className="font-bold text-lg text-slate-800">{incidentTitle}</h3>
          <p className="text-slate-500 text-sm flex items-center gap-1 mt-1">
            <Calendar size={14} /> 
            Clôture le : {new Date(endDate).toLocaleDateString("fr-FR")} à {new Date(endDate).toLocaleTimeString("fr-FR", {hour: '2-digit', minute:'2-digit'})}
          </p>
        </div>
        <div className="flex items-center gap-2 bg-indigo-50 text-indigo-700 px-4 py-2 rounded-lg font-bold text-lg">
          <Euro size={20} />
          {amount.toLocaleString("fr-FR")}
        </div>
      </div>

      {/* Actions */}
      <div className="p-5 bg-slate-50">
        <p className="text-sm text-slate-600 mb-4 font-medium">Exprimez votre avis sur ces travaux :</p>
        
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <button
            onClick={() => handleVote("FOR")}
            disabled={isLoading}
            className="flex items-center justify-center gap-2 py-3 px-4 rounded-lg font-semibold bg-green-600 hover:bg-green-700 text-white transition disabled:opacity-50"
          >
            <Check size={18} />
            POUR
          </button>

          <button
            onClick={() => handleVote("AGAINST")}
            disabled={isLoading}
            className="flex items-center justify-center gap-2 py-3 px-4 rounded-lg font-semibold bg-red-600 hover:bg-red-700 text-white transition disabled:opacity-50"
          >
            <X size={18} />
            CONTRE
          </button>

          <button
            onClick={() => handleVote("ABSTAIN")}
            disabled={isLoading}
            className="flex items-center justify-center gap-2 py-3 px-4 rounded-lg font-semibold bg-slate-200 hover:bg-slate-300 text-slate-700 transition disabled:opacity-50"
          >
            <Minus size={18} />
            ABSTENTION
          </button>
        </div>
      </div>
    </div>
  );
}