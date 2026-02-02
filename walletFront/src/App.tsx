import { createBrowserRouter, RouterProvider } from "react-router";
import { useEffect } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Home } from "./public/home";

const routes = [
  {
    path: "/",
    element: <Home />,
  },
];
function App() {
  useEffect(() => {
    fetch(
      `${import.meta.env.VITE_GATEWAY_BASE ?? "http://localhost:8082"}/auth/csrf`,
      {
        credentials: "include",
        method: "GET",
      },
    )
      .then(() => console.log("Handshake CSRF réussi"))
      .catch((err) => console.error("Échec handshake", err));
  }, []);
  const router = createBrowserRouter(routes);
  const queryClient = new QueryClient();
  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  );
}

export default App;
