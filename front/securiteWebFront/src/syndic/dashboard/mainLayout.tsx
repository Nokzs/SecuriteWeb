import { Outlet } from "react-router"
import { MainNavBarSyndic } from "../component/mainNavBar"





export const MainLayoutSyndicDashboard = () => {
    return (
        <div className="flex min-h-screen">
            <MainNavBarSyndic />
            <main className="flex-1 bg-white p-8 overflow-y-auto">
                <Outlet />
            </main>
        </div>
    )
}
