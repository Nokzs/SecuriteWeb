import { useState } from "react";
import { type expense, ExpenseRow } from "./expenseRow";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useParams } from "react-router";
import { API_BASE } from "../../config/urls";
import type { Page } from "../../types/pagination";
import { useSecureFetch } from "../../hooks/secureFetch";
import { PaginationController } from "../../component/PaginationController";

const ExpenseList = () => {
  const { buildingId } = useParams();
  const secureFetch = useSecureFetch();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [filter, setFilter] = useState({ page: 0, limit: 10 });
  const mutation = useMutation({
    mutationFn: async (data: { description: string; amount: number }) => {
      const res = await secureFetch(`${API_BASE}/expense/${buildingId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });
      if (!res.ok) throw new Error("Failed to create expense");
    },
  });
  const {
    data: expense,
    isLoading,
    refetch,
  } = useQuery<Page<expense>>({
    queryFn: async () => {
      const res = await secureFetch(
        `${API_BASE}/expense/${buildingId}?page=${filter.page}&limit=${filter.limit}`,
      );
      if (!res.ok) throw new Error("Failed to fetch expenses");
      return await res.json();
    },
    queryKey: ["buildingExpenses", buildingId, filter],
  });

  const handleSuccess = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const data = Object.fromEntries(formData.entries());

    await mutation.mutateAsync({
      amount: parseFloat(data.amount as string),
      description: data.label as string,
    });
    refetch();
  };
  if (isLoading)
    return (
      <div className="flex items-center justify-center h-64">Chargement...</div>
    );
  console.log(expense);
  return (
    <div className="max-w-5xl mx-auto p-8">
      {/* Header */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-800">
          Dépenses du Bâtiment
        </h1>
        <button
          onClick={() => setIsModalOpen(true)}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-medium transition-all shadow-sm flex items-center gap-2"
        >
          <span>+ Nouvelle facture</span>
        </button>
      </div>

      {/* Table & Pagination */}
      <div className="bg-white rounded-xl shadow-sm border overflow-hidden">
        <table className="w-full text-left">
          <thead className="bg-gray-50 border-b">
            <tr>
              <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase">
                Date
              </th>
              <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase">
                Détails
              </th>
              <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase text-right">
                Montant
              </th>
              <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase text-center">
                Statut
              </th>
              <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase text-center">
                Payants
              </th>
              <th className="px-6 py-4 text-xs font-semibold text-gray-500 uppercase text-right">
                Action
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {expense?.content?.map((exp: expense, i) => (
              <ExpenseRow
                key={i}
                expense={exp}
                onCancelSuccess={() => refetch()}
              />
            ))}
          </tbody>
        </table>
        {expense && (
          <div className="border-t bg-gray-50 px-4">
            <PaginationController
              pageData={expense}
              onPageChange={(newPage) =>
                setFilter((prev) => ({ ...prev, page: newPage }))
              }
            />
          </div>
        )}
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div
            className="absolute inset-0 bg-black/50 backdrop-blur-sm"
            onClick={() => setIsModalOpen(false)}
          ></div>

          <div className="relative bg-white rounded-2xl shadow-xl w-full max-w-lg p-8 mx-4">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-xl font-bold text-gray-900">
                Nouvelle Facture
              </h2>
              <button
                onClick={() => setIsModalOpen(false)}
                className="text-gray-400 hover:text-gray-600 text-2xl"
              ></button>
            </div>

            <form className="space-y-4" onSubmit={(e) => handleSuccess(e)}>
              <div>
                <label className="block text-sm font-medium text-black mb-1">
                  Désignation
                </label>
                <input
                  name="label"
                  type="text"
                  className="w-full border rounded-lg p-2.5 focus:ring-2 text-black focus:ring-blue-500 outline-none"
                  placeholder="Ex: Réparation ascenseur"
                />
              </div>

              <div className="grid grid-cols-1 gap-4">
                <div>
                  <label className="block text-sm font-medium text-black mb-1">
                    Montant (€)
                  </label>
                  <input
                    name="amount"
                    type="number"
                    className="w-full border rounded-lg p-2.5 focus:ring-2 text-black focus:ring-blue-500 outline-none"
                    placeholder="0.00"
                    min="0.01"
                    step="0.01"
                    required
                    onKeyDown={(e) => {
                      if (e.key === "-" || e.key === "e") {
                        e.preventDefault();
                      }
                    }}
                  />
                </div>
              </div>

              <div className="pt-4 flex gap-3">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="flex-1 px-4 py-2.5 border rounded-lg text-gray-700 hover:bg-gray-50 font-medium transition-colors"
                >
                  Annuler
                </button>
                <input
                  type="submit"
                  className="flex-1 px-4 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition-colors"
                  value="Enregistrer"
                />
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ExpenseList;
