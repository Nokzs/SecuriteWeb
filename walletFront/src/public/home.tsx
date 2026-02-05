import { Wallet, ArrowUpRight, ArrowDownLeft, Plus } from "lucide-react";
import { useState } from "react";
import { AddMoneyModal } from "../component/AddMoneyModal";
import { LogoutButton } from "../component/logoutButton";
import { useMutation, useQuery } from "@tanstack/react-query";
import { userStore } from "../store/userStore";
import { useSecureFetch } from "../hooks/secureFetch";
import { API_BASE } from "../config/urls";
import type { Page } from "../types/pagination";
import { OperationCard } from "../component/OperationCard";
import { PaginationController } from "../component/paginationController";
type user = {
  email: string;
  role: string;
  balance: number;
};

export type Operation = {
  mail: string;
  amount: number;
  date: Date;
  sign: string;
  label: string;
  receiverMail: string;
};

export const Home = () => {
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const user = userStore((s) => s.user)?.uuid;
  const secureFetch = useSecureFetch();
  const [filter, setFilter] = useState<{
    page: number;
    limit: number;
  }>({ page: 0, limit: 10 });
  const userData = useQuery<user | null>({
    queryKey: ["user", user],
    queryFn: async () => {
      const user = await secureFetch(`${API_BASE}/user`);
      if (user.ok) {
        return user.json();
      }
      return null;
    },
  });
  const page = useQuery<Page<Operation> | null>({
    queryKey: ["transactions", filter, user],
    queryFn: async () => {
      const response = await secureFetch(
        `${API_BASE}/operation?page=${filter.page}&limit=${filter.limit}`,
      );
      if (response.ok) {
        return response.json();
      }
      return null;
    },
  });
  const mutation = useMutation({
    mutationFn: async (amount: number) => {
      const response = await secureFetch(`${API_BASE}/user/addMoney`, {
        method: "POST",
        body: JSON.stringify({ amount }),
      });
    },
  });
  const addMoney = (amount: number) => {
    mutation.mutate(amount, {
      onSuccess: () => {
        userData.refetch();
        page.refetch();
        setIsOpen(false);
      },
    });
  };
  console.log(page.data);
  return (
    <div className="flex justify-center items-center min-h-screen bg-slate-50 p-4">
      <div className="w-full max-w-md bg-white  shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95 duration-500">
        <div className="p-8  bg-linear-to-b from-indigo-600 to-violet-700 text-white relative overflow-hidden">
          <div className="relative z-20 flex justify-between items-start mb-8">
            <div className="p-3 bg-white/20 backdrop-blur-md rounded-2xl">
              <Wallet className="h-6 w-6 text-white" />
            </div>
            <LogoutButton />
          </div>

          {/* Cercles décoratifs en arrière-plan */}
          <div className="absolute -right-10 -bottom-10 w-40 h-40 bg-white/10 rounded-full blur-3xl"></div>
          <div className="absolute -left-10 -top-10 w-40 h-40 bg-indigo-400/20 rounded-full blur-2xl"></div>
        </div>

        {/* Corps du Wallet */}
        <div className="p-6">
          {/* Infos Utilisateur */}
          <div className="flex items-start  gap-4 p-4 bg-slate-50 rounded-2xl border border-slate-100 mb-6">
            <div className="flex items-center gap-4 p-4 bg-slate-50 rounded-2xl border border-slate-100 mb-6 ">
              <div className="h-12 w-12 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 font-bold text-lg">
                {userData.data?.email.charAt(0).toUpperCase()}
              </div>
              <div className="flex-1 overflow-hidden">
                <p className="text-xs text-slate-500 font-medium uppercase tracking-tighter">
                  Compte actif
                </p>
                <p className="text-slate-800 font-semibold truncate">
                  {userData.data?.email}
                </p>
              </div>
            </div>

            <div>
              <span className="text-sm text-slate-500">Solde</span>
              <p className="text-2xl font-bold text-slate-800">
                {userData.data?.balance}
              </p>
            </div>
          </div>
          {userData.data && userData.data.role === "PROPRIETAIRE" && (
            <div className="grid grid-cols-3 gap-4 mb-8">
              <button
                onClick={() => setIsOpen(true)}
                className="flex flex-col items-center gap-2 group outline-none"
              >
                <div className="h-14 w-14 rounded-2xl bg-slate-50 flex items-center justify-center text-slate-600 group-hover:bg-indigo-50 group-hover:text-indigo-600 transition-all border border-slate-100 group-hover:border-indigo-100 group-active:scale-90">
                  <Plus className="h-6 w-6" />
                </div>
                <span className="text-xs font-semibold text-slate-500 group-hover:text-indigo-600">
                  Ajouter
                </span>
              </button>
            </div>
          )}

          {/* Dernières transactions (Optionnel pour le look) */}
          <div className="space-y-4">
            <h3 className="text-sm font-bold text-slate-800 px-1">
              Activité récente
            </h3>
            {page.data && page.data.content.length > 0 ? (
              <>
                {page.data.content.map((operation, index) => (
                  <OperationCard key={index} operation={operation} />
                ))}

                <PaginationController
                  pageData={page.data}
                  onPageChange={(newPage) =>
                    setFilter({ ...filter, page: newPage })
                  }
                />
              </>
            ) : (
              <div className="p-8 text-center text-gray-400 text-sm">
                Aucune opération pour le moment.
              </div>
            )}{" "}
          </div>
        </div>
      </div>
      <AddMoneyModal
        isOpen={isOpen}
        onClose={() => setIsOpen(false)}
        onConfirm={(e) => {
          addMoney(e);
        }}
      />
    </div>
  );
};
