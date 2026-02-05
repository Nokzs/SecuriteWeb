import { LogOut } from "lucide-react";
export const LogoutButton = () => {
  const gatewayBase =
    import.meta.env.VITE_GATEWAY_BASE ?? "http://localhost:8082";

  const handleLogout = () => {
    const form = document.createElement("form");
    form.method = "POST";
    // On garde l'URL propre
    form.action = `${gatewayBase}/auth/logout`;

    // 1. Ajout du paramètre "app" (DYNAMIQUE)
    const appInput = document.createElement("input");
    appInput.type = "hidden";
    appInput.name = "app"; // Doit correspondre à properties.getParam() dans ton Java
    appInput.value = "appB";
    form.appendChild(appInput);

    // 2. Ajout du CSRF Token
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
    <button
      onClick={handleLogout}
      className="relative z-30 p-2 hover:bg-white/10 rounded-full transition-colors cursor-pointer"
    >
      <LogOut className="h-5 w-5 opacity-80" />
    </button>
  );
};
