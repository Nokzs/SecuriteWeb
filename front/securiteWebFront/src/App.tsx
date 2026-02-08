import { createBrowserRouter, Navigate, RouterProvider } from "react-router";
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
import { FirstOwnerLogin } from "./owner/FirstOwnerLogin";
import { SyndicMessages } from "./syndic/messages/syndicMessages";
import { MainLayoutOwnerDashboard } from "./owner/dashboard/mainLayoutOwner";
import { SyndicReclamations } from "./syndic/reclamations/syndicReclamations";
import { OwnerProperties } from "./owner/ownerProperties";
import ExpenseList from "./syndic/expense/expenseList";
import { GATEWAY_BASE } from "./config/urls";
import PaymentRequestList from "./owner/invoice/InvoiceList";
import { OwnerIncidents } from "./owner/OwnerIncidents";
import { SyndicVotes } from "./syndic/votes/SyndicVotes";
import { AdminDashboard } from "./admin/AdminDashboard";
import { MainLayoutAdmin } from "./admin/dashboard/MainLayoutAdmin";

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
        path: "/register",
        element: <Register />,
      },
    ],
  },
  {
    id: "auth",
    element: <AuthRoute />,
    middleware: [],
    children: [
      {
        element: <RoleRoute allowedRoles={["SYNDIC"]} redirectPath="/" />,
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
                    path: "expenses",
                    element: <ExpenseList />,
                  },
                ],
              },
              {
                element: <MainLayoutSyndicDashboard />,
                children: [
                  {
                    index: true,
                    element: <Navigate to="building" replace />, // Par de9faut on va sur les apparts
                  },
                  {
                    path: "building",
                    element: <BuildingsList />,
                  },
                  {
                    path: "messages",
                    element: <SyndicMessages />,
                  },
                  {
                    path: "reclamations",
                    element: <SyndicReclamations />,
                  },
                ],
              },
              {
                path: "votes",
                element: <SyndicVotes />,
              }
            ],
          },
        ],
      },
      {
        element: <RoleRoute allowedRoles={["PROPRIETAIRE"]} redirectPath="/" />,
        children: [
          {
            path: "owner/first-login",
            element: <FirstOwnerLogin />,
          },
          {
            path: "owner",
            element: <MainLayoutOwnerDashboard />, // Le Layout avec la Navbar latérale
            children: [
              {
                index: true,
                element: <Navigate to="properties" replace />, // Redirection par défaut
              },
              {
                path: "properties",
                element: <OwnerProperties />, // Le contenu (Grille + Modale)
              },
              // { path: "incidents", element: <OwnerIncidents /> }
              { path: "invoices", element: <PaymentRequestList /> },
              {
                path: "incidents",
                element: <OwnerIncidents />
              }
            ],
          },
        ],
      },
      {
        element: <RoleRoute allowedRoles={["ADMIN"]} redirectPath="/" />,
        children: [
          {
            path: "admin",
            element: <MainLayoutAdmin />, // Le Layout avec la barre latérale Admin
            children: [
              {
                index: true,
                element: <AdminDashboard />, // Le Dashboard avec les stats
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
