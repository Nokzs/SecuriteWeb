import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import { authService, fetchUserInfo } from "./ssoAuthService";
import { userStore } from "../../store/userStore";

const Callback = () => {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (!params.get("code")) {
      userStore.getState().clearUser();
      navigate("/login", { replace: true });
      return;
    }

    authService
      .completeLogin()
      .then(async (oidcUser) => {
        if (!oidcUser?.access_token) {
          throw new Error("Access token missing from callback");
        }
        console.log("Callback successful, access token obtained.");
        userStore.getState().setToken(oidcUser.access_token);

        let parsedUser = userStore.getState().get(oidcUser.access_token);

        if (!parsedUser) {
          // Tentative via `id_token` (souvent JWT) sinon via `/userinfo`.
          if (oidcUser.id_token) {
            parsedUser = userStore.getState().get(oidcUser.id_token);
          }
        }

        let role = parsedUser?.role;
        let isFirstLogin = parsedUser?.isFirstLogin;

        if (!role) {
          const userInfo = (await fetchUserInfo(oidcUser.access_token)) as {
            role?: string;
            isFirstLogin?: boolean;
            isFirstTime?: boolean;
            uuid?: string;
          };

          role = userInfo.role;
          // compat backend: `isFirstTime` vs `isFirstLogin`
          isFirstLogin = userInfo.isFirstLogin ?? userInfo.isFirstTime;
        }

        if (role === "SYNDIC") {
          navigate("/syndic", { replace: true });
          return;
        }

        if (role === "PROPRIETAIRE") {
          if (isFirstLogin) {
            navigate("/owner/first-login", { replace: true });
            return;
          }

          navigate("/owner", { replace: true });
          return;
        }

        navigate("/", { replace: true });
      })
      .catch((err) => {
        console.error("Échec de la validation du callback :", err);
        userStore.getState().clearUser();
        navigate("/login", { replace: true });
      });
  }, [navigate, location.search]);

  return <div>Vérification sécurisée en cours...</div>;
};

export default Callback;
