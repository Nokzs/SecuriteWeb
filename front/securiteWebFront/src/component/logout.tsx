import { useSecureFetch } from "../hooks/secureFetch";
export const LogoutButton = () => {
  const secureFetch = useSecureFetch();
  const gatewayBase =
    import.meta.env.VITE_GATEWAY_BASE ?? "http://localhost:8082";

  const handleLogout = async () => {
    try {
      // On fait l'appel POST vers la Gateway
      // On passe ?app=appA pour que ta config Java sache où rediriger
      const response = await secureFetch(
        `${gatewayBase}/auth/logout?app=appA`,
        {
          method: "POST",
        },
      );

      if (response.ok) {
        // TRÈS IMPORTANT : Le logout OIDC renvoie une redirection (302) vers le SSO.
        // fetch ne redirige pas automatiquement la page entière.
        // On récupère l'URL du SSO renvoyée par la Gateway et on y envoie le navigateur.
        if (response.redirected) {
          window.location.href = response.url;
        } else {
          // Si la Gateway répond 200 sans redirect (config spécifique), on redirige nous-mêmes
          window.location.assign("/");
        }
      }
    } catch (error) {
      console.error("Erreur lors de la déconnexion", error);
    }
  };

  return (
    <div className="border-t border-slate-200 pt-4">
      <button
        onClick={handleLogout}
        className="w-full flex items-center px-4 py-3 text-red-600 hover:bg-red-50 rounded-lg transition-colors font-medium"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          className="h-5 w-5 mr-3"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
          />
        </svg>
        Déconnexion
      </button>
    </div>
  );
};
