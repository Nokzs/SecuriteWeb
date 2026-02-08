import { useMutation } from "@tanstack/react-query";
import { useSecureFetch } from "../../hooks/secureFetch";
import { API_BASE } from "../../config/urls";
export type expense = {
  id: string;
  label: string;
  amount: number;
  status: string;
  createdAt: string;
  numberPaid: number;
  invoiceCount: number;
};
type ExpenseRowProps = {
  expense: expense;
  onCancelSuccess: (id: string) => void;
};
export const ExpenseRow = ({ expense, onCancelSuccess }: ExpenseRowProps) => {
  const secureFetch = useSecureFetch();
  console.log("Rendering ExpenseRow for:", expense);
  const cancelMutation = useMutation({
    mutationFn: async (id: string) => {
      const res = await secureFetch(`${API_BASE}/expense/${id}`, {
        method: "PUT",
      });
      if (!res.ok) {
        const errorData = await res.text();
        throw new Error(errorData || "Erreur lors de l'annulation");
      }
    },
  });
  const handleCancel = async () => {
    if (!window.confirm("Confirmer l'annulation ?")) return;
    await cancelMutation.mutateAsync(expense.id);
    onCancelSuccess(expense.id);
  };
  const isCancelled = expense.status === "CANCELLED";

  if (cancelMutation.isPending) {
    return (
      <div className="fixed inset-0 z-100 flex flex-col items-center justify-center bg-gray-900/60 backdrop-blur-sm">
        <div className="bg-white p-8 rounded-xl shadow-2xl flex flex-col items-center">
          <svg
            className="animate-spin h-12 w-12 text-blue-600 mb-4"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              className="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              strokeWidth="4"
            ></circle>
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            ></path>
          </svg>
          <h3 className="text-lg font-semibold text-gray-900">
            annulation en cours...
          </h3>
          <p className="text-sm text-gray-500 mt-1">
            Veuillez ne pas actualiser la page...
          </p>
        </div>
      </div>
    );
  }
  return (
    <tr
      className={`hover:bg-gray-50 transition-colors ${isCancelled ? "opacity-50" : ""}`}
    >
      <td className="px-6 py-4 text-sm text-gray-600">
        {new Date(expense.createdAt).toLocaleDateString("fr-FR")}
      </td>
      <td className="px-6 py-4">
        <div className="text-sm font-medium text-gray-900">{expense.label}</div>
      </td>
      <td className="px-6 py-4 text-right font-semibold text-gray-900">
        {expense.amount.toFixed(2)} €
      </td>
      <td className="px-6 py-4 text-center">
        <span
          className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
            !isCancelled
              ? "bg-green-100 text-green-800"
              : "bg-red-100 text-red-800"
          }`}
        >
          {!isCancelled ? expense.status : "Annulée"}
        </span>
      </td>
      <td className="px-6 py-4 text-right font-semibold text-gray-900">
        {expense.numberPaid} / {expense.invoiceCount}
      </td>
      <td className="px-6 py-4 text-right">
        {!isCancelled && (
          <button
            onClick={handleCancel}
            disabled={cancelMutation.isPending}
            className="inline-flex items-center px-3 py-1.5 text-xs font-medium rounded shadow-sm text-white bg-red-600 hover:bg-red-700 disabled:bg-gray-400 transition-all"
          >
            {cancelMutation.isPending ? "En cours..." : "Annuler"}
          </button>
        )}
      </td>
    </tr>
  );
};
