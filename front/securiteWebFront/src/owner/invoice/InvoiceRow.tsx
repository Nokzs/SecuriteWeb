import { useMutation } from "@tanstack/react-query";
import { API_BASE } from "../../config/urls";
import { useSecureFetch } from "../../hooks/secureFetch";

export type PaymentRequest = {
  id: string;
  label: string;
  amount: number;
  createdAt: string;
  statut: "PENDING" | "PAID" | "CANCELLED";
};

interface Props {
  payment: PaymentRequest;
  onActionSuccess: () => void;
}
const statusStyles = {
  PAID: "bg-green-100 text-green-800",
  PENDING: "bg-yellow-100 text-yellow-800",
  CANCELLED: "bg-red-100 text-red-800",
};
export const PaymentRequestRow = ({ payment, onActionSuccess }: Props) => {
  const secureFetch = useSecureFetch();
  const paiement = useMutation({
    mutationFn: async () => {
      await new Promise((resolve) => setTimeout(resolve, 2000));
      const res = await secureFetch(`${API_BASE}/invoices/pay/${payment.id}`, {
        method: "POST",
      });
      if (!res.ok) {
        const errorData = await res.text();
        throw new Error(errorData || "Erreur lors du paiement");
      }
    },
    onSuccess: () => {
      onActionSuccess();
    },
    onError: (error) => {
      alert(error instanceof Error ? error.message : "Erreur inconnue");
    },
  });
  const handlePay = () => {
    paiement.mutate();
  };

  if (paiement.isPending) {
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
            Paiement en cours
          </h3>
          <p className="text-sm text-gray-500 mt-1">
            Veuillez ne pas actualiser la page...
          </p>
        </div>
      </div>
    );
  }
  return (
    <tr className="hover:bg-gray-50 transition-colors">
      <td className="px-6 py-4 text-sm text-gray-600">
        {new Date(payment.createdAt).toLocaleDateString("fr-FR")}
      </td>
      <td className="px-6 py-4">
        <div className="text-sm font-medium text-gray-900">{payment.label}</div>
      </td>
      <td className="px-6 py-4 text-right font-semibold text-gray-900">
        {payment.amount.toFixed(2)} €
      </td>
      <td className="px-6 py-4 text-center">
        <span
          className={`px-2.5 py-0.5 rounded-full text-xs font-medium ${statusStyles[payment.statut]}`}
        >
          {payment.statut === "PAID"
            ? "Payée"
            : payment.statut === "PENDING"
              ? "En attente"
              : "Annulée"}
        </span>
      </td>
      <td className="px-6 py-4 text-right">
        {payment.statut === "PENDING" && (
          <button
            onClick={handlePay}
            className="text-blue-600 hover:text-blue-800 text-sm font-medium"
          >
            Payer
          </button>
        )}
      </td>
    </tr>
  );
};
