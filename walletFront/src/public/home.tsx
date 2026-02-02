export function Home() {
  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        height: "100vh",
      }}
    >
      <a
        href={`${import.meta.env.VITE_GATEWAY_BASE ?? "http://localhost:8082"}/login?app=appB`}
        className="text-gray-700 hover:text-blue-600 font-medium transition"
      >
        Connexion
      </a>
    </div>
  );
}
