import { useSecureFetch } from "../hooks/secureFetch";
import { userStore } from "../store/userStore";
import { API_BASE } from "../config/urls";
type NavBarProps = {
  children?: React.ReactNode;
};
export const NavBar = ({ children }: NavBarProps) => {
  const reset = userStore((s) => s.clearUser);
  const secureFetch = useSecureFetch();
  const handleLogout = async () => {
    await secureFetch(`${API_BASE}/auth/logout`, {
      method: "POST",
    });
    await fetch(
      `${import.meta.env.VITE_GATEWAY_BASE ?? "http://localhost:8082"}/auth/csrf`,
      {
        credentials: "include",
        method: "GET",
      },
    )
      .then(() => console.log("Handshake CSRF réussi"))
      .catch((err) => console.error("Échec handshake", err));
    reset();
  };
  return (
    <nav className="w-64 h-screen flex flex-col bg-slate-50 border-r border-slate-200 p-6 shadow-sm">
      <div className="mb-10 px-2">
        <h1 className="text-xl font-bold text-indigo-600">
          Gestionnaire Syndic
        </h1>
      </div>
      {children}
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
    </nav>
  );
};
