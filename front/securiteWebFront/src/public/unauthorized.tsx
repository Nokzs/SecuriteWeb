import { Link } from "react-router-dom";

import { LOGIN_URL } from "../config/urls";
import { userStore } from "../store/userStore";

export function Unauthorized() {
  const clearUser = userStore((s) => s.clearUser);

  return (
    <div className="p-6 max-w-xl mx-auto">
      <h1 className="text-2xl font-semibold">Accès refusé</h1>
      <p className="mt-2">
        Ton compte est connecté, mais tu n’as pas les droits nécessaires pour accéder à
        cette page.
      </p>

      <div className="mt-6 flex gap-3 flex-wrap">
        <Link className="btn btn-ghost" to="/">
          Retour à l’accueil
        </Link>

        <button
          className="btn btn-primary"
          onClick={() => {
            clearUser();
            window.location.assign(LOGIN_URL);
          }}
        >
          Se reconnecter
        </button>
      </div>

      <p className="mt-6 text-sm opacity-70">
        Si tu penses que c’est une erreur, contacte un administrateur.
      </p>
    </div>
  );
}
