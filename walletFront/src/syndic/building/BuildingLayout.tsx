import { Outlet } from "react-router"
import { BuildingNavBar } from "./buildingNavBar"





export const BuildingLayoutSyndicDashboard = () => {
    return (
        <div className="flex min-h-screen">
            <BuildingNavBar/>
            <main className="flex-1 bg-white p-8 overflow-y-auto">
                <Outlet />
            </main>
        </div>
    )
}
