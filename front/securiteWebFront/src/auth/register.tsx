import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useMutation } from "@tanstack/react-query";
import { useSecureFetch } from "../hooks/secureFetch";
import { useNavigate } from "react-router";
import { API_BASE } from "../config/urls";
const registerSchema = z.object({
  email: z.string().min(1, { message: "L'email est requis" }),
  password: z
    .string()
    .min(10, "Le mot de passe doit contenir au moins 10 caractères")
    .max(50, "Le mot de passe est trop long")
    .regex(/[A-Z]/, "Il faut au moins une majuscule")
    .regex(/[a-z]/, "Il faut au moins une minuscule")
    .regex(/[0-9]/, "Il faut au moins un chiffre")
    .regex(/[@$!%*?&]/, "Il faut au moins un caractère spécial (@$!%*?&)"),
  nomAgence: z.string().min(1, {
    message: "Veuillez donnez le nom de votre agence",
  }),
  adresse: z
    .string()
    .min(1, { message: "Saissisez l'adresse de votre syndic" }),
  telephone: z.string().regex(/^\+?[0-9]{10,15}$/, "Numéro invalide"),
});
type RegisterFormValues = z.infer<typeof registerSchema>;
export const Register = () => {
  const secureFetch = useSecureFetch();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    mode: "onSubmit",
    defaultValues: {
      email: "",
      password: "",
      telephone: "",
      adresse: "",
      nomAgence: "",
    },
  });
  const { mutateAsync } = useMutation({
    mutationFn: async (data: RegisterFormValues) => {
      const response = await secureFetch(`${API_BASE}/auth/register`, {
        method: "POST",
        body: JSON.stringify(data),
      });
      if (response.ok) {
        return false;
      }
      return true;
    },
  });
  const navigate = useNavigate();
  const onSubmit = async (data: RegisterFormValues) => {
    const user = await mutateAsync(data);
    if (user) {
      const gatewayBase =
        import.meta.env.VITE_GATEWAY_BASE ?? "http://localhost:8082";
      window.location.assign(
        `${gatewayBase}/oauth2/authorization/gateway-client`,
      );
    }
  };
  return (
    <div className="flex justify-center items-center min-h-screen bg-slate-50 p-4">
      <div className="w-full max-w-lg bg-white rounded-2xl shadow-xl border border-slate-200 p-8 animate-in fade-in zoom-in-95 duration-300">
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-bold text-slate-800">
            Créer un compte Syndic
          </h1>
          <p className="text-slate-500 mt-2">
            Enregistrez votre agence pour commencer
          </p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label
              htmlFor="email"
              className="block text-sm font-medium text-slate-700 mb-1"
            >
              Email
            </label>
            <input
              id="email"
              type="email"
              {...register("email")}
              className={`w-full text-black border p-3 rounded-xl outline-none transition-all ${
                errors.email
                  ? "border-red-400 focus:ring-2 focus:ring-red-100"
                  : "border-slate-300 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              }`}
              placeholder="contact@agence.com"
            />
            {errors.email && (
              <p className="mt-1 text-xs text-red-500">
                {errors.email.message}
              </p>
            )}
          </div>

          <div>
            <label
              htmlFor="password"
              className="block text-sm font-medium text-slate-700 mb-1"
            >
              Mot de passe
            </label>
            <input
              id="password"
              type="password"
              {...register("password")}
              className={`w-full text-black border p-3 rounded-xl outline-none transition-all ${
                errors.password
                  ? "border-red-400 focus:ring-2 focus:ring-red-100"
                  : "border-slate-300 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              }`}
              placeholder="••••••••"
            />
            {errors.password && (
              <p className="mt-1 text-xs text-red-500">
                {errors.password.message}
              </p>
            )}
          </div>

          <hr className="border-slate-100 my-2" />

          <div>
            <label
              htmlFor="nomAgence"
              className="block text-sm font-medium text-slate-700 mb-1"
            >
              Nom de l'agence
            </label>
            <input
              id="nomAgence"
              type="text"
              {...register("nomAgence")}
              className={`w-full border text-black p-3 rounded-xl outline-none transition-all ${
                errors.nomAgence
                  ? "border-red-400 focus:ring-2 focus:ring-red-100"
                  : "border-slate-300 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              }`}
              placeholder="Syndic Immobilier Pro"
            />
            {errors.nomAgence && (
              <p className="mt-1 text-xs text-red-500">
                {errors.nomAgence.message}
              </p>
            )}
          </div>

          <div>
            <label
              htmlFor="adresse"
              className="block text-sm font-medium text-black  mb-1"
            >
              Adresse
            </label>
            <input
              id="adresse"
              type="text"
              {...register("adresse")}
              className={`w-full border p-3 rounded-xl outline-none transition-all ${
                errors.adresse
                  ? "border-red-400 focus:ring-2 focus:ring-red-100"
                  : "border-slate-300 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              }`}
              placeholder="12 rue des Lilas, 75000 Paris"
            />
            {errors.adresse && (
              <p className="mt-1 text-xs text-red-500">
                {errors.adresse.message}
              </p>
            )}
          </div>

          <div>
            <label
              htmlFor="telephone"
              className="block text-sm font-medium text-slate-700 mb-1"
            >
              Téléphone
            </label>
            <input
              id="telephone"
              type="text"
              {...register("telephone")}
              className={`w-full border p-3 rounded-xl outline-none transition-all ${
                errors.telephone
                  ? "border-red-400 focus:ring-2 focus:ring-red-100"
                  : "border-slate-300 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              }`}
              placeholder="01 23 45 67 89"
            />
            {errors.telephone && (
              <p className="mt-1 text-xs text-red-500">
                {errors.telephone.message}
              </p>
            )}
          </div>

          {/* Bouton */}
          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-indigo-600 text-white font-bold py-3 rounded-xl hover:bg-indigo-700 shadow-lg shadow-indigo-100 transition-all disabled:opacity-50 disabled:cursor-not-allowed mt-6"
          >
            {isSubmitting ? (
              <span className="flex items-center justify-center gap-2">
                <svg
                  className="animate-spin h-5 w-5 text-white"
                  viewBox="0 0 24 24"
                >
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                    fill="none"
                  />
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  />
                </svg>
                Création du compte...
              </span>
            ) : (
              "Créer mon compte"
            )}
          </button>

          <p className="text-center text-sm text-slate-500 mt-4">
            Déjà inscrit ?{" "}
            <button
              onClick={() => navigate("/login")}
              type="button"
              className="text-indigo-600 font-semibold hover:underline"
            >
              Se connecter
            </button>
          </p>
        </form>
      </div>
    </div>
  );
};
