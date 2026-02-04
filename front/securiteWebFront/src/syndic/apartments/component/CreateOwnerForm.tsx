import { useState } from "react";
import { useForm } from "react-hook-form";
import { useMutation } from "@tanstack/react-query";
import { UserPlus, X, Loader2, Copy, CheckCircle2 } from "lucide-react";
import { useSecureFetch } from "../../../hooks/secureFetch";

import { API_BASE } from "../../../config/urls";
type OwnerFormValues = {
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
};
const generateEasyPassword = () => {
  const sets = {
    upper: "ABCDEFGHJKLMNPQRSTUVWXYZ",
    lower: "abcdefghijkmnpqrstuvwxyz",
    number: "23456789",
    special: "!@#$%",
  };

  const passwordArray = [
    sets.upper[
      window.crypto.getRandomValues(new Uint32Array(1))[0] % sets.upper.length
    ],
    sets.lower[
      window.crypto.getRandomValues(new Uint32Array(1))[0] % sets.lower.length
    ],
    sets.number[
      window.crypto.getRandomValues(new Uint32Array(1))[0] % sets.number.length
    ],
    sets.special[
      window.crypto.getRandomValues(new Uint32Array(1))[0] % sets.special.length
    ],
  ];

  const allChars = Object.values(sets).join("");
  const extraChars = Array.from(
    window.crypto.getRandomValues(new Uint32Array(6)),
  ).map((x) => allChars[x % allChars.length]);

  const finalArray = [...passwordArray, ...extraChars];

  return finalArray
    .map((value) => ({
      value,
      sort: window.crypto.getRandomValues(new Uint32Array(1))[0],
    }))
    .sort((a, b) => a.sort - b.sort)
    .map(({ value }) => value)
    .join("");
};
export const CreateOwnerForm = ({
  initialEmail,
  onSuccess,
  onCancel,
}: {
  initialEmail: string;
  onSuccess: (owner: OwnerFormValues) => void;
  onCancel: () => void;
}) => {
  const secureFetch = useSecureFetch();
  const [credentials, setCredentials] = useState<{
    email: string;
    password: string;
  } | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: { email: initialEmail, prenom: "", nom: "", telephone: "" },
  });

  const mutation = useMutation({
    mutationFn: async (data: OwnerFormValues) => {
      const password = generateEasyPassword();

      const response = await secureFetch(`${API_BASE}/auth/owner`, {
        method: "POST",
        body: JSON.stringify({ ...data, password }),
        headers: { "Content-Type": "application/json" },
      });

      if (!response.ok) throw new Error("Échec de la création");

      return { ...data, password };
    },
    onSuccess: (data) => {
      setCredentials({ email: data.email, password: data.password });
    },
  });

  const onSubmit = (data: OwnerFormValues) => {
    mutation.mutate(data);
  };

  if (credentials && mutation.data) {
    return (
      /* Overlay de fond pour assombrir le reste de l'écran */
      <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
        {/* Ta Modal centrée */}
        <div className="bg-white p-8 rounded-2xl border-2 border-indigo-100 shadow-2xl space-y-6 animate-in zoom-in-95 duration-200 max-w-md w-full relative">
          <div className="text-center space-y-2">
            <div className="inline-flex p-3 bg-green-100 rounded-full text-green-600 mb-2">
              <CheckCircle2 size={32} />
            </div>
            <h3 className="text-xl font-bold text-slate-800">
              Propriétaire créé !
            </h3>
            <p className="text-sm text-slate-500">
              Veuillez transmettre ce mot de passe temporaire au propriétaire.
              Il ne sera plus jamais affiché.
            </p>
          </div>

          <div className="relative group">
            <div className="bg-slate-50 border-2 border-slate-200 rounded-xl p-4 flex items-center justify-between">
              <code className="text-2xl font-mono font-bold text-indigo-600 tracking-widest">
                {credentials.email}
              </code>
              <code className="text-2xl font-mono font-bold text-indigo-600 tracking-widest">
                {credentials.password}
              </code>
              <button
                onClick={() =>
                  navigator.clipboard.writeText(credentials.password)
                }
                className="p-2 hover:bg-indigo-100 rounded-lg text-indigo-400 transition-colors"
                title="Copier"
              >
                <Copy size={20} />
              </button>
            </div>
          </div>

          <button
            onClick={() => {
              setCredentials(null);
              onSuccess(mutation.data);
              return;
            }}
            className="w-full bg-slate-900 text-white py-3 rounded-xl font-bold hover:bg-black transition-all shadow-lg"
          >
            J'ai noté le mot de passe
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <form
        onSubmit={handleSubmit(onSubmit)}
        className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xl space-y-4 animate-in fade-in slide-in-from-bottom-4 w-full max-w-md relative"
      >
        <div className="flex justify-between items-center border-b pb-3">
          <h3 className="text-lg font-bold text-slate-800 flex items-center gap-2">
            <UserPlus size={20} className="text-indigo-600" />
            Nouveau Propriétaire
          </h3>
          <button
            type="button"
            onClick={onCancel}
            className="text-slate-400 hover:text-slate-600 p-1"
          >
            <X size={20} />
          </button>
        </div>

        <div className="grid grid-cols-2 gap-4 text-black">
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-600">Prénom</label>
            <input
              {...register("prenom", { required: "Obligatoire" })}
              className={`w-full rounded-lg text-black border-slate-300 focus:ring-indigo-500 ${errors.prenom ? "border-red-500" : ""}`}
            />
          </div>
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-600">Nom</label>
            <input
              {...register("nom", { required: "Obligatoire" })}
              className={`w-full rounded-lg text-black border-slate-300 focus:ring-indigo-500 ${errors.nom ? "border-red-500" : ""}`}
            />
          </div>
        </div>

        <div className="space-y-1">
          <label className="text-sm font-medium text-slate-600">E-mail</label>
          <input
            {...register("email")}
            className="w-full rounded-lg text-black border-slate-200 bg-slate-50 text-black pointer-coarse:"
          />
        </div>

        <div className="space-y-1 text-black">
          <label className="text-sm font-medium text-slate-600">
            Téléphone
          </label>
          <input
            {...register("telephone")}
            placeholder="+33 6..."
            className="w-full rounded-lg text-black border-slate-300 focus:ring-indigo-500"
          />
        </div>

        <div className="flex gap-3 pt-2">
          <button
            type="submit"
            disabled={mutation.isPending}
            className="flex-1 bg-indigo-600 text-white py-3 rounded-xl font-bold hover:bg-indigo-700 transition-all shadow-md disabled:opacity-50"
          >
            {mutation.isPending ? (
              <Loader2 className="animate-spin mx-auto" size={20} />
            ) : (
              "Créer le compte"
            )}
          </button>
        </div>
      </form>
    </div>
  );
};
