import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useMutation } from "@tanstack/react-query";
import { useSecureFetch } from "../hooks/secureFetch";
import { NavLink, useNavigate } from "react-router";
import { userStore, type UserStoreType } from "../store/userStore";
const API_URL = import.meta.env.VITE_APIURL;
const loginSchema = z.object({
  email: z.string().min(1, { message: "L'email est requis" }),
  password: z.string().min(1, { message: "Le mot de passe est requis" }),
});
type LoginFormValues = z.infer<typeof loginSchema>;
export const Login = () => {
  const setToken = userStore((s: UserStoreType) => s.setToken);

  const secureFetch = useSecureFetch();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    mode: "onSubmit",
    defaultValues: {
      email: "",
      password: "",
    },
  });
  const { mutateAsync, isError, error, reset } = useMutation({
    mutationFn: async (data: LoginFormValues) => {
      const formData = new URLSearchParams();
      formData.append("email", data.email);
      formData.append("password", data.password);

      const response = await secureFetch(`${API_URL}/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: formData.toString(),
      });

      if (!response.ok) {
        let errorMessage = "Erreur de connexion";
        try {
          const errorData = await response.json();
          errorMessage = errorData.message || errorMessage;
          throw(errorMessage);
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        } catch (_) {
          
                   errorMessage = `Erreur ${response.status}: ${response.statusText}`;
        }
        throw new Error(errorMessage); // C'est ce throw qui active isError
      }
      return response.json();
    },
    onError: (error: Error) => {
      console.error("Erreur lors de la mutation de connexion :", error.message);
    },
  });
  const navigate = useNavigate();
  const onSubmit = async (data: LoginFormValues) => {
    // On ne met pas de try/catch ici si on veut que useMutation garde l'état d'erreur
    const user = await mutateAsync(data);
    setToken(user.access);

    if (user.access) {
      const parsedUser = userStore.getState().get(user.access);
      if (parsedUser?.role === "PROPRIETAIRE") {
        navigate("/owner", { replace: true });
        return;
      }
      if (parsedUser?.role === "SYNDIC") {
        navigate("/syndic", { replace: true });
        return;
      }
    }

    navigate("/", { replace: true });
  };
  return (
    <div className="flex justify-center items-center h-screen bg-slate-50">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-xl border border-slate-200 p-8 animate-in fade-in zoom-in-95 duration-300">
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-bold text-slate-800">Bienvenue</h1>
          <p className="text-slate-500 mt-2">
            Veuillez vous connecter à votre compte
          </p>
        </div>

        <form
          onSubmit={handleSubmit(onSubmit)}
          onChange={() => isError && reset()}
          className="space-y-5"
        >
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
              placeholder="nom@exemple.com"
            />
            {errors.email && (
              <p className="mt-1 text-sm text-red-500 flex items-center gap-1">
                <span>⚠</span> {errors.email.message}
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
              className={`w-full border p-3 rounded-xl text-black outline-none transition-all ${
                errors.password
                  ? "border-red-400 focus:ring-2 focus:ring-red-100"
                  : "border-slate-300 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              }`}
              placeholder="••••••••"
            />
            {errors.password && (
              <p className="mt-1 text-sm text-red-500 flex items-center gap-1">
                <span>⚠</span> {errors.password.message}
              </p>
            )}
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-indigo-600 text-white font-bold py-3 rounded-xl hover:bg-indigo-700 shadow-lg shadow-indigo-100 transition-all disabled:opacity-50 disabled:cursor-not-allowed mt-4"
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
                Connexion...
              </span>
            ) : (
              "Se connecter"
            )}
          </button>
          {isError && (
            <div className="p-3 mb-4 text-sm text-red-700 bg-red-100 rounded-xl border border-red-200 animate-in slide-in-from-top-2">
              <span className="font-bold">Erreur :</span> {error.message}
            </div>
          )}
          <p className="text-center text-sm text-slate-500 mt-6">
            Pas encore de compte ?{" "}
            <NavLink
              to="/register"
              className="text-indigo-600 font-semibold hover:underline"
            >
              S'inscrire
            </NavLink>
          </p>
        </form>
      </div>
    </div>
  );
};
