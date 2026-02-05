export const LogoutButton = () => {
  const gatewayBase =
    import.meta.env.VITE_GATEWAY_BASE ?? "http://localhost:8082";

  const handleLogout = () => {
    const form = document.createElement("form");
    form.method = "POST";
    form.action = `${gatewayBase}/auth/logout?app=appA`;

    const csrfToken = document.cookie
      .split("; ")
      .find((row) => row.startsWith("XSRF-TOKEN="))
      ?.split("=")[1];

    if (csrfToken) {
      const input = document.createElement("input");
      input.type = "hidden";
      input.name = "_csrf";
      input.value = decodeURIComponent(csrfToken);
      form.appendChild(input);
    }

    document.body.appendChild(form);
    form.submit();
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
