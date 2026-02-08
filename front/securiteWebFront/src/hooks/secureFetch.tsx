import Cookies from "js-cookie";
import { useCallback } from "react";
import { useNavigate } from "react-router"; // ou "react-router-dom"
import { userStore } from "../store/userStore"; // Assure-toi du chemin

export const useSecureFetch = () => {
  const navigate = useNavigate();
  // On récupère l'action pour nettoyer le token
  const setToken = userStore((state) => state.setToken);

  const secureFetch = useCallback(
    async (url: string, options: RequestInit = {}) => {
      // 1. Initialisation des headers
      const headers = new Headers(options.headers);

      if (!headers.has("Content-Type")) {
        headers.set("Content-Type", "application/json");
      }

      // 2. Récupération du token CSRF
      const csrfToken = Cookies.get("XSRF-TOKEN");
      
      const method = options.method?.toUpperCase() || "GET";
      const isMutation = ["POST", "PUT", "DELETE", "PATCH"].includes(method);

      if (isMutation && csrfToken) {
        headers.set("X-XSRF-TOKEN", csrfToken);
      }

      const defaultOptions: RequestInit = {
        ...options,
        credentials: "include", // Important pour le JSESSIONID
        headers: headers,
      };

      try {
        const response = await fetch(url, defaultOptions);

        if (response.status === 401) {
          console.warn("Session invalide ou compte supprimé. Déconnexion forcée.");
          
          // 1. On vide le store (l'utilisateur n'est plus connecté pour React)
          setToken(null); 
          
          // 2. On redirige vers le login immédiatement
          navigate("/login", { replace: true });
          
          // 3. On arrête tout ici (on ne renvoie même pas la réponse pour éviter les erreurs de parsing)
          return response; 
        }

        // Gestion optionnelle du 403 (Accès interdit mais connecté)
        if (response.status === 403) {
          alert("Vous n'avez pas les droits pour effectuer cette action.");}

        return response;
      } catch (error) {
        console.error("Erreur réseau critique :", error);
        throw error;
      }
    },
    [navigate, setToken]
  );

  return secureFetch;
};