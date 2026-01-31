import { UserManager } from "oidc-client-ts";

import {
  AUTHORITY,
  CLIENT_ID,
  REDIRECT_URI,
  SCOPES,
  TOKEN_ENDPOINT,
} from "../../config/urls";

const userManager = new UserManager({
  authority: AUTHORITY,
  client_id: CLIENT_ID,
  redirect_uri: REDIRECT_URI,
  response_type: "code",
  scope: SCOPES,
  loadUserInfo: false,
});

export async function fetchUserInfo(accessToken: string): Promise<unknown> {
  // `oidc-client-ts` peut charger userinfo, mais on ajoute une méthode
  // explicite pour récupérer les claims côté front.
  const response = await fetch(`/userinfo`, {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  if (!response.ok) {
    throw new Error(`userinfo failed: ${response.status}`);
  }

  return await response.json();
}

export const authService = {
  login: () => userManager.signinRedirect(),

  completeLogin: async () => {
    return await userManager.signinRedirectCallback();
  },

  getUser: () => userManager.getUser(),

  logout: () => userManager.signoutRedirect(),
};

export async function refreshAccessToken(): Promise<string> {
  const body = new URLSearchParams();
  body.set("grant_type", "refresh_token");
  body.set("client_id", CLIENT_ID);

  const response = await fetch(TOKEN_ENDPOINT, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: body.toString(),
  });

  if (!response.ok) {
    throw new Error(`Refresh failed: ${response.status}`);
  }

  const payload = (await response.json()) as { access_token?: string };
  if (!payload.access_token) {
    throw new Error("Refresh response missing access_token");
  }

  return payload.access_token;
}

