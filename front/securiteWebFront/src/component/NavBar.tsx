import { LogoutButton } from "./logout";

type NavBarProps = {
  children?: React.ReactNode;
};

export const NavBar = ({ children }: NavBarProps) => {
  return (
    <nav className="w-64 h-screen flex flex-col bg-slate-50 border-r border-slate-200 p-6 shadow-sm">
      <div className="mb-10 px-2">
        <h1 className="text-xl font-bold text-indigo-600">
          Gestionnaire Syndic
        </h1>
      </div>
      {children}
      <LogoutButton />
    </nav>
  );
};
