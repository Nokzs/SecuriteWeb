const required = (name: string, value: string | undefined): string => {
  if (!value) {
    throw new Error(`Missing environment variable: ${name}`);
  }
  return value;
};

export const AUTHORITY = required(
  "VITE_AUTHORITY",
  import.meta.env.VITE_AUTHORITY,
);

export const API_BASE = required("VITE_APIURL", import.meta.env.VITE_APIURL);

export const CLIENT_ID = required(
  "VITE_CLIENT_ID",
  import.meta.env.VITE_CLIENT_ID,
);

export const REDIRECT_PATH = required(
  "VITE_REDIRECT_PATH",
  import.meta.env.VITE_REDIRECT_PATH,
);

export const REDIRECT_URI = `${window.location.origin}${REDIRECT_PATH}`;

export const TOKEN_ENDPOINT = `/oauth2/token`;

export const SCOPES = import.meta.env.VITE_SCOPES ?? "openid profile";
