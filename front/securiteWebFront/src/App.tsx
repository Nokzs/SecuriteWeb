import { createBrowserRouter, RouterProvider } from "react-router";
import { Login } from "./auth/login";
import { useEffect } from "react";
import { Register } from "./auth/register";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Outlet } from "react-router-dom";
import { MainLayoutSyndicDashboard } from "./syndic/dashboard/mainLayout";
import { BuildingLayoutSyndicDashboard } from "./syndic/building/BuildingLayout";
import { BuildingsList } from "./syndic/building/buildingList";
import { PublicRoute } from "./auth/PublicRoute";
import { AuthRoute } from "./auth/AuthRoute";
import { RoleRoute } from "./auth/RoleRoute";

const routes = [
  {
    id: "public",
    element: <PublicRoute />,
    children: [
      {
        path: "/",
        id: "root",
        element: <>Welcome</>,
      },
      {
        path: "/login",
        element: <Login />,
      },
      {
        path: "/register",
        element: <Register />,
      },
    ],
  },
  {
    id: "auth",
    //element: <AuthRoute />,
    children: [
      {
        // element: <RoleRoute allowedRoles={["SYNDIC"]} redirectPath="/owner" />,
        children: [
          {
            path: "syndic",
            element: <Outlet />,
            children: [
              {
                path: "building/:buildingId",
                element: <BuildingLayoutSyndicDashboard />,

                children: [
                  {
                    path: "appartments",
                    element: <>Apartments Syndic</>,
                  },
                  {
                    path: "residents",
                    element: <>Residents Syndic</>,
                  },
                ],
              },
              {
                element: <MainLayoutSyndicDashboard />,
                children: [
                  {
                    index: true,
                    element: <div>Liste des immeubles syndic</div>,
                  },
                  {
                    path: "building",
                    element: <BuildingsList />,
                  },
                  {
                    path: "reclamations",
                    element: <>Reclamations Syndic</>,
                  },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
];
function App() {
  useEffect(() => {
    fetch(`${import.meta.env.vite_apiurl}/auth/csrf`, {
      headers: {
        credentials: "include",
      },
    });
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
