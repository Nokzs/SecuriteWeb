import { createBrowserRouter, RouterProvider } from "react-router";
import { useEffect } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Home } from "./public/home";
import { AuthRoute } from "./route/AuthRoute";
import { GATEWAY_BASE } from "./config/urls";

const routes = [
  {
    path: "/",
    element: <AuthRoute />,
    children: [
      {
        index: true,
        element: <Home />,
      },
    ],
  },
];
function App() {
  useEffect(() => {
    fetch(`${GATEWAY_BASE}/auth/csrf`, {
      credentials: "include",
      method: "GET",
    })
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
