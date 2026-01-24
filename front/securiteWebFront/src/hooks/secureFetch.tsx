import Cookies from "js-cookie";
import { useCallback } from "react";

export const useSecureFetch = () => {
  // On type 'options' avec RequestInit pour avoir l'autocomplétion native de fetch
  const secureFetch = useCallback(
    async (url: string, options: RequestInit = {}) => {
      // Initialisation des headers de manière sécurisée pour TypeScript
      const headers = new Headers(options.headers);

      // On définit le Content-Type par défaut s'il n'existe pas déjà
      if (!headers.has("Content-Type")) {
        headers.set("Content-Type", "application/json");
      }

      const defaultOptions: RequestInit = {
        ...options,
        credentials: "include",
        headers: headers,
      };

      // 2. Récupération du token CSRF
      const csrfToken = Cookies.get("XSRF-TOKEN");

      // 3. Ajout du header CSRF pour les méthodes de mutation
      const method = options.method?.toUpperCase() || "GET";
      const isMutation = ["POST", "PUT", "DELETE", "PATCH"].includes(method);

      if (isMutation && csrfToken) {
        // Utiliser l'objet Headers est plus propre que de manipuler un objet litéral
        (defaultOptions.headers as Headers).set("X-XSRF-TOKEN", csrfToken);
      }

      try {
        const response = await fetch(url, defaultOptions);

        if (response.status === 403) {
          console.error("Erreur CSRF ou accès refusé (403)");
        }

        return response;
      } catch (error) {
        console.error("Erreur réseau :", error);
        throw error;
      }
    },
    [],
  );

  return secureFetch;
};
