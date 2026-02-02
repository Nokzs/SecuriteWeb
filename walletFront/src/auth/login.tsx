import { authService } from "./sso/ssoAuthService";

export const Login = () => {
  return (
    <div className="flex justify-center items-center h-screen bg-slate-50">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-xl border border-slate-200 p-8 animate-in fade-in zoom-in-95 duration-300">
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-bold text-slate-800">Bienvenue</h1>
          <p className="text-slate-500 mt-2">Veuillez vous connecter via le SSO</p>
        </div>

        <button
          type="button"
          onClick={() => authService.login()}
          className="w-full bg-indigo-600 text-white font-bold py-3 rounded-xl hover:bg-indigo-700 shadow-lg shadow-indigo-100 transition-all mt-4"
        >
          Se connecter
        </button>
      </div>
    </div>
  );
};
