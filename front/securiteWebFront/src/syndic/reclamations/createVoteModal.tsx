import { useState } from "react";
import { X, AlertCircle } from "lucide-react";

interface CreateVoteModalProps {
  incidentTitle: string;
  buildingName: string;
  onClose: () => void;
  onSubmit: (data: { amount: number; endDate: string }) => Promise<void>;
}

export function CreateVoteModal({
  incidentTitle,
  buildingName,
  onClose,
  onSubmit,
}: CreateVoteModalProps) {
  const [amount, setAmount] = useState<string>("");
  
  // --- 1. FONCTION UTILITAIRE POUR LA DATE ---
  // Calcule une date par défaut (ex: Aujourd'hui + 7 jours) au format "YYYY-MM-DDTHH:mm"
  const getDefaultDate = (daysToAdd = 7) => {
    const date = new Date();
    date.setDate(date.getDate() + daysToAdd);

    date.setMinutes(date.getMinutes() - date.getTimezoneOffset()); 
    return date.toISOString().slice(0, 16);
  };

  // --- 2. INITIALISATION DU STATE ---
  const [endDate, setEndDate] = useState<string>(getDefaultDate(7));
  
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Pour l'attribut min (empêcher dates passées)
  const minDate = getDefaultDate(0); 

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const amountValue = parseFloat(amount);

    if (isNaN(amountValue) || amountValue <= 0) {
      setError("Le montant doit être supérieur à 0.");
      return;
    }

    if (!endDate) {
      setError("La date de fin est obligatoire.");
      return;
    }

    try {
      setIsLoading(true);
      await onSubmit({ amount: amountValue, endDate });
    } catch (err) {
      setError("Une erreur est survenue lors de la création du vote.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 animate-in fade-in duration-200">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden">
        
        <div className="bg-indigo-600 p-4 flex justify-between items-center text-white">
          <h3 className="font-bold text-lg">Lancer un vote</h3>
          <button onClick={onClose} className="hover:bg-indigo-700 p-1 rounded-full transition">
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          
          <div className="bg-blue-50 p-3 rounded-lg text-sm text-blue-800 mb-4">
            <p className="font-semibold">Concerne : {incidentTitle}</p>
            <p className="opacity-80 text-xs mt-1">Immeuble : {buildingName}</p>
          </div>

          {error && (
            <div className="bg-red-50 text-red-600 p-3 rounded-lg text-sm flex items-center gap-2">
              <AlertCircle size={16} />
              {error}
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Montant à payer par lot (€)
            </label>
            <input
              type="number"
              step="0.01"
              min="0"
              placeholder="ex: 150.00"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 transition"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Date limite de vote
            </label>
            {/* L'input est maintenant pré-rempli. 
                L'utilisateur voit une date valide dès l'ouverture.
            */}
            <input
              type="datetime-local"
              min={minDate}
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 transition"
              required
            />
            <p className="text-xs text-slate-500 mt-1">
              Date proposée par défaut : dans 7 jours.
            </p>
          </div>

          <div className="flex justify-end gap-3 mt-6">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-slate-600 hover:bg-slate-100 rounded-lg transition font-medium"
              disabled={isLoading}
            >
              Annuler
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition font-medium flex items-center gap-2"
              disabled={isLoading}
            >
              {isLoading ? "Création..." : "Valider le vote"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}