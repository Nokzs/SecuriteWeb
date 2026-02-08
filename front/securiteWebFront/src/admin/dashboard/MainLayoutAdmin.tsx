import { Outlet } from "react-router-dom";
import { MainNavBarAdmin } from "../MainNavBarAdmin";

export const MainLayoutAdmin = () => {
    return (
        <div className="flex min-h-screen bg-slate-50">
            {/* Barre latérale fixe à gauche */}
            <MainNavBarAdmin />
            
            {/* Zone de contenu dynamique */}
            <main className="flex-1 p-8 overflow-y-auto h-screen">
                <div className="max-w-7xl mx-auto">
                    <Outlet />
                </div>
            </main>
        </div>
    );
};