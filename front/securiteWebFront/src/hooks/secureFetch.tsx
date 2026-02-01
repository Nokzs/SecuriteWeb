import Cookies from "js-cookie";
import { useCallback } from "react";

import { refreshAccessToken } from "../auth/sso/ssoAuthService";
import { TOKEN_ENDPOINT } from "../config/urls";
import { userStore } from "../store/userStore";

let refreshPromise: Promise<string> | null = null;

const waitForRefresh = async () => {
  if (!refreshPromise) {
    refreshPromise = refreshAccessToken().finally(() => {
      refreshPromise = null;
    });
  }

  return refreshPromise;
};

export const useSecureFetch = () => {
  const secureFetch = useCallback(
    async (url: string, options: RequestInit = {}) => {
      const headers = new Headers(options.headers);

      if (!headers.has("Content-Type")) {
        headers.set("Content-Type", "application/json");
      }

      const token = userStore.getState().user;
      if (token && !headers.has("Authorization")) {
        headers.set("Authorization", `Bearer ${token}`);
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

        if (url.startsWith(TOKEN_ENDPOINT)) {
          return response;
        }

        const newAccessToken = await waitForRefresh();
        headers.set("Authorization", `Bearer ${newAccessToken}`);

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
