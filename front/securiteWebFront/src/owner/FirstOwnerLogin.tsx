import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import * as z from "zod";
import { useSecureFetch } from "../hooks/secureFetch";
import { userStore } from "../store/userStore";

const API_URL = import.meta.env.VITE_APIURL;

const changePasswordSchema = z
  .object({
    password: z
      .string()
      .min(10, "Le mot de passe doit contenir au moins 10 caractères")
      .max(50, "Le mot de passe est trop long")
      .regex(/[A-Z]/, "Il faut au moins une majuscule")
      .regex(/[a-z]/, "Il faut au moins une minuscule")
      .regex(/[0-9]/, "Il faut au moins un chiffre")
      .regex(
        /[@$!%*?&]/,
        "Il faut au moins un caractère spécial (@$!%*?&)",
      ),
    confirmPassword: z.string().min(1, "Veuillez confirmer le mot de passe"),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Les mots de passe ne correspondent pas",
    path: ["confirmPassword"],
  });

type ChangePasswordFormValues = z.infer<typeof changePasswordSchema>;

export function FirstOwnerLogin() {
  const user = userStore((s) => s.user);
  const get = userStore((s) => s.get);
  const setToken = userStore((s) => s.setToken);
  const parsedUser = user ? get(user) : null;

  const secureFetch = useSecureFetch();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ChangePasswordFormValues>({
    resolver: zodResolver(changePasswordSchema),
    mode: "onSubmit",
    defaultValues: { password: "", confirmPassword: "" },
  });

  const { mutateAsync, isError, error } = useMutation({
    mutationFn: async (data: ChangePasswordFormValues) => {
      const response = await secureFetch(`${API_URL}/auth/change-password`, {
        method: "POST",
        body: JSON.stringify({ newPassword: data.password }),
      });

      if (!response.ok) {
        let errorMessage = "Erreur lors du changement de mot de passe";
        try {
          const errorData = await response.json();
          errorMessage = errorData.message || errorMessage;
        } catch {
          errorMessage = `Erreur ${response.status}: ${response.statusText}`;
        }
        throw new Error(errorMessage);
      }

      return response.json();
    },
  });

  const onSubmit = async (data: ChangePasswordFormValues) => {
    const result = await mutateAsync(data);

    if (result?.access) {
      setToken(result.access);
    }

    navigate("/owner", { replace: true });
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-lg bg-white rounded-2xl shadow-xl border border-slate-200 p-8">
        <h1 className="text-2xl font-bold text-slate-800">
          Première connexion
        </h1>
        <p className="text-slate-600 mt-3">
          Pour des raisons de sécurité, vous devez changer votre mot de passe.
        </p>

        <div className="mt-6 rounded-xl border border-slate-200 bg-slate-50 p-4 text-slate-700 text-sm">
          <div>Compte : {parsedUser?.uuid ?? "inconnu"}</div>
          <div>Rôle : {parsedUser?.role ?? "inconnu"}</div>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4">
          <div>
            <label
              htmlFor="password"
              className="block text-sm font-medium text-slate-700 mb-1"
            >
              Nouveau mot de passe
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
              <p className="mt-1 text-xs text-red-500">{errors.password.message}</p>
            )}
          </div>

          <div>
            <label
              htmlFor="confirmPassword"
              className="block text-sm font-medium text-slate-700 mb-1"
            >
              Répéter le mot de passe
            </label>
            <input
              id="confirmPassword"
              type="password"
              {...register("confirmPassword")}
              className={`w-full border p-3 rounded-xl text-black outline-none transition-all ${
                errors.confirmPassword
                  ? "border-red-400 focus:ring-2 focus:ring-red-100"
                  : "border-slate-300 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              }`}
              placeholder="••••••••"
            />
            {errors.confirmPassword && (
              <p className="mt-1 text-xs text-red-500">
                {errors.confirmPassword.message}
              </p>
            )}
          </div>

          {isError && (
            <div className="p-3 text-sm text-red-700 bg-red-100 rounded-xl border border-red-200">
              <span className="font-bold">Erreur :</span> {error.message}
            </div>
          )}

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-indigo-600 text-white font-bold py-3 rounded-xl hover:bg-indigo-700 shadow-lg shadow-indigo-100 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isSubmitting ? "Enregistrement..." : "Changer mon mot de passe"}
          </button>
        </form>
      </div>
    </div>
  );
}
