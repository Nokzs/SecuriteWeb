import React from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";

const loginSchema = z.object({
  email: z
    .string()
    .min(1, { message: "L'email est requis" })
    .email({ message: "Format d'email invalide" }),
  password: z
    .string()
    .min(6, { message: "Le mot de passe doit contenir au moins 6 caractères" }),
});
type LoginFormValues = z.infer<typeof loginSchema>;
export const Login = () => {
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

  const onSubmit = async (data: LoginFormValues) => {
    try {
      console.log("Données envoyées :", data);
      await new Promise((resolve) => setTimeout(resolve, 1000));
    } catch (error) {
      console.error("Erreur de connexion", error);
    }
  };

  return (
    <div className="flex justify-center items-center h-screen">
      <fieldset className="fieldset bg-base-200 border-base-300 rounded-box w-xs border p-4">
        <legend className="fieldset-legend">Login</legend>
        <form onSubmit={handleSubmit(onSubmit)}>
          {/* Champ Email */}
          <div style={{ marginBottom: "1rem" }}>
            <label htmlFor="email" className="label">
              Email
            </label>
            <input
              id="email"
              type="email"
              {...register("email")}
              className="input"
              placeholder="Email"
            />
            {errors.email && (
              <p style={{ color: "red", fontSize: "0.8rem" }}>
                {errors.email.message}
              </p>
            )}
          </div>

          {/* Champ Mot de passe */}
          <div style={{ marginBottom: "1rem" }}>
            <label htmlFor="password" className="label">
              Mot de passe
            </label>
            <input
              placeholder="password"
              id="password"
              type="password"
              className="input"
              {...register("password")}
            />
            {errors.password && (
              <p style={{ color: "red", fontSize: "0.8rem" }}>
                {errors.password.message}
              </p>
            )}
          </div>

          <button
            type="submit"
            className="btn btn-neutral mt-4"
            disabled={isSubmitting}
          >
            {isSubmitting ? "Connexion en cours..." : "Se connecter"}
          </button>
        </form>
      </fieldset>
    </div>
  );
};
