import { createBrowserRouter, Navigate, RouterProvider } from "react-router";
import { Login } from "./auth/login";
import { useEffect } from "react";
import { Register } from "./auth/register";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Outlet } from "react-router-dom";
import { MainLayoutSyndicDashboard } from "./syndic/dashboard/mainLayout";
import { BuildingLayoutSyndicDashboard } from "./syndic/building/BuildingLayout";
import { BuildingsList } from "./syndic/building/buildingList";
import { PublicRoute } from "./auth/PublicRoute";
import { RoleRoute } from "./auth/RoleRoute";
import { AuthRoute } from "./auth/AuthRoute";
import { ApartmentList } from "./syndic/apartments/component/apartmentsList";
import { Home } from "./public/home";

const routes = [
  {
    id: "public",
    element: <PublicRoute />,
    children: [
      {
        path: "/",
        id: "root",
        element: <Home />,
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
    element: <AuthRoute />,
    children: [
      {
        element: <RoleRoute allowedRoles={["SYNDIC"]} redirectPath="/owner" />,
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
                    path: "apartments",
                    element: <ApartmentList />,
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
                    element: <Navigate to="building" replace />, // Par défaut on va sur les apparts
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
    fetch(`${import.meta.env.VITE_APIURL}/auth/csrf`, {
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
