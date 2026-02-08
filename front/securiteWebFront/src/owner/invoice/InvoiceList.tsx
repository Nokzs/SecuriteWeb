import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { API_BASE } from "../../config/urls";
import type { Page } from "../../types/pagination";
import { useSecureFetch } from "../../hooks/secureFetch";
import { PaginationController } from "../../component/PaginationController";
import { PaymentRequestRow, type PaymentRequest } from "./InvoiceRow";

const PaymentRequestList = () => {
  const secureFetch = useSecureFetch();
  const [filter, setFilter] = useState({ page: 0, limit: 10 });

  const {
    data: payments,
    isLoading,
    refetch,
  } = useQuery<Page<PaymentRequest>>({
    queryFn: async () => {
      const res = await secureFetch(
        `${API_BASE}/invoices?page=${filter.page}&limit=${filter.limit}`,
      );
      if (!res.ok) throw new Error("Failed to fetch payments");
      return await res.json();
    },
    queryKey: ["buildingPayments", filter],
  });

  if (isLoading)
    return (
      <div className="flex items-center justify-center h-64 text-gray-500 font-medium">
        Chargement des demandes de paiement...
      </div>
    );

  return (
    <div className="max-w-6xl mx-auto p-8">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Demandes de Paiement
          </h1>
          <p className="text-sm text-gray-500">
            Suivi des factures envoyées aux propriétaires
          </p>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <table className="w-full text-left">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-4 text-xs font-semibold text-gray-400 uppercase">
                Date
              </th>
              <th className="px-6 py-4 text-xs font-semibold text-gray-400 uppercase">
                Libellé
              </th>
              <th className="px-6 py-4 text-xs font-semibold text-gray-400 uppercase text-right">
                Montant
              </th>
              <th className="px-6 py-4 text-xs font-semibold text-gray-400 uppercase text-center">
                Statut
              </th>
              <th className="px-6 py-4 text-xs font-semibold text-gray-400 uppercase text-right">
                Action
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {payments?.content?.map((pay, i) => (
              <PaymentRequestRow
                key={i}
                payment={pay}
                onActionSuccess={() => refetch()}
              />
            ))}
          </tbody>
        </table>

        {payments && (
          <div className="border-t bg-gray-50/50">
            <PaginationController
              pageData={payments}
              onPageChange={(newPage) =>
                setFilter((prev) => ({ ...prev, page: newPage }))
              }
            />
          </div>
        )}
      </div>
    </div>
  );
};

export default PaymentRequestList;
