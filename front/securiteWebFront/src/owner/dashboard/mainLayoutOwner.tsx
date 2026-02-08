import { Outlet } from "react-router";
import { MainNavBarOwner } from "../mainNavBarOwner";

export const MainLayoutOwnerDashboard = () => {
    return (
        <div className="flex min-h-screen bg-slate-50">
            <MainNavBarOwner />
            <main className="flex-1 p-8 overflow-y-auto h-screen">
                <Outlet />
            </main>
        </div>
    );
};