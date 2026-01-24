import { createBrowserRouter, RouterProvider } from "react-router";
import { Login } from "./auth/login";

const routes = [
  {
    path: "/",
    id: "root",
    element: <>welcome</>,
  },
  {
    path: "/login",
    id: "login",
    element: <Login />,
  },
];

function App() {
  const router = createBrowserRouter(routes);
  return <RouterProvider router={router} />;
}

export default App;
