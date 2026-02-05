import Cookies from "js-cookie";
import { useCallback } from "react";

import { userStore } from "../store/userStore";

export const useSecureFetch = () => {
  const secureFetch = useCallback(
    async (url: string, options: RequestInit = {}) => {
      const headers = new Headers(options.headers);

      if (!headers.has("Content-Type")) {
        headers.set("Content-Type", "application/json");
      }

      const defaultOptions: RequestInit = {
        ...options,
        credentials: "include",
        headers,
      };

      const csrfToken = Cookies.get("XSRF-TOKEN");
      const method = options.method?.toUpperCase() || "GET";
      const isMutation = ["POST", "PUT", "DELETE", "PATCH"].includes(method);

      if (isMutation && csrfToken) {
        headers.set("X-XSRF-TOKEN", csrfToken);
      }

      const doFetch = async () => fetch(url, defaultOptions);

      try {
        const response = await doFetch();

        if (response.status !== 401) {
          if (response.status === 403) {
            console.error("Erreur CSRF ou accès refusé (403)");
          }
          return response;
        }

        return await doFetch();
      } catch (error) {
        userStore.getState().clearUser();
        window.location.assign("/login");
        throw error;
      }
    },
    [],
  );

  return secureFetch;
};
