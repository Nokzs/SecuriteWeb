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
        console.log("Callback OIDC User :", oidcUser);
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
