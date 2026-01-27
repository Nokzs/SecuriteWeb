import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { UserPlus, Search, Loader2 } from "lucide-react";
import { CreateOwnerForm } from "./CreateOwnerForm";
import { useSecureFetch } from "../../../hooks/secureFetch";
type OwnerSearchByEmailProps = {
  setOwnerEmail: (email: string) => void;
};

export const OwnerSearchByEmail = ({
  setOwnerEmail,
}: OwnerSearchByEmailProps) => {
  const [email, setEmail] = useState("");
  const [showResult, setShowResult] = useState(true);
  const secureFetch = useSecureFetch();
  const API_URL = import.meta.env.VITE_APIURL;
  const [showCreateOwnerModal, setShowCreateOwnerModal] = useState(false);
  const {
    data: foundOwner,
    refetch,
    isFetching,
    isError,
    fetchStatus,
  } = useQuery({
    queryKey: ["ownerSearch", email],
    queryFn: async () => {
      const res = await secureFetch(`${API_URL}/user/by-email?email=${email}`);
      if (!res.ok) throw new Error("Owner not found");
      return res.json();
    },
    enabled: false,
    retry: false,
  });

  const handleSearch = async () => {
    if (email.includes("@")) {
      await refetch();
      setShowResult(true);
    }
  };

  const isNotFound = isError && fetchStatus === "idle";
  const select = (email: string) => {
    setOwnerEmail(email);
    setShowResult(false);
    setEmail("");
  };
  return (
    <div className="space-y-4 p-4 border rounded-xl bg-slate-50">
      <label className="block text-sm font-medium text-slate-700">
        Rechercher un propriétaire par e-mail
      </label>

      <div className="flex gap-2">
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="flex-1 rounded-lg border-slate-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 p-2 text-black"
          placeholder="ex: jean.martin@email.com"
        />
        <button
          onClick={handleSearch}
          disabled={isFetching || !email.includes("@")}
          className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors disabled:opacity-50 flex items-center gap-2"
        >
          {isFetching ? (
            <Loader2 className="animate-spin" size={18} />
          ) : (
            <Search size={18} />
          )}
          Vérifier
        </button>
      </div>

      {showResult && foundOwner && !isFetching && (
        <div className="flex items-center justify-between p-3 bg-green-50 border border-green-200 rounded-lg animate-in fade-in slide-in-from-top-1">
          <div>
            <p className="font-bold text-green-800">
              {foundOwner.prenom} {foundOwner.nom}
            </p>
            <p className="text-sm text-green-600">Propriétaire identifié</p>
          </div>
          <button
            onClick={() => select(foundOwner.email)}
            className="text-sm bg-green-600 text-white px-3 py-1 rounded shadow-sm hover:bg-green-700 transition-colors"
          >
            Sélectionner
          </button>
        </div>
      )}

      {isNotFound && (
        <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg flex items-center justify-between animate-in fade-in slide-in-from-top-1">
          <p className="text-sm text-amber-700">
            Aucun compte trouvé pour cet e-mail.
          </p>
          <button
            onClick={() => {
              setShowCreateOwnerModal(true);
            }}
            className="flex items-center text-sm font-bold text-indigo-600 hover:text-indigo-800"
          >
            <UserPlus size={16} className="mr-1" /> Créer un profil
          </button>
        </div>
      )}

      {showCreateOwnerModal && (
        <CreateOwnerForm
          initialEmail={email || ""}
          onCancel={() => setShowCreateOwnerModal(false)}
          onSuccess={(owner) => setOwnerEmail(owner.email)}
        />
      )}
    </div>
  );
};
