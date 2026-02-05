const required = (name: string, value: string | undefined): string => {
  if (!value) {
    throw new Error(`Missing environment variable: ${name}`);
  }
  return value;
};

export const GATEWAY_BASE = required(
  "VITE_GATEWAY_BASE",
  import.meta.env.VITE_GATEWAY_BASE,
);

export const APP_NAME = import.meta.env.VITE_APP_NAME ?? "appA";
export const API_BASE = `${GATEWAY_BASE}/${APP_NAME}/api`;

export const CSRF_ENDPOINT = `${GATEWAY_BASE}/auth/csrf`;
export const LOGIN_URL = `${GATEWAY_BASE}/login?app=${APP_NAME}`;
export const SCOPES = import.meta.env.VITE_SCOPES ?? "openid profile";
export const COPRO_URL =
  import.meta.env.VITE_COPRO_URL ?? "http://localhost:3001";
