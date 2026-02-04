import React from "react";
import { formatDistanceToNow } from "date-fns";
import { fr } from "date-fns/locale";
import { ArrowUpRight, ArrowDownLeft, PlusCircle } from "lucide-react";
import type { Operation } from "../public/home";
export const OperationCard = ({ operation }: { operation: Operation }) => {
  const isPositive = operation.sign === "+";
  const isSelfTransfer =
    !operation.receiverMail || operation.receiverMail === operation.mail;

  const relativeDate = operation.date
    ? formatDistanceToNow(new Date(operation.date), {
        addSuffix: true,
        locale: fr,
      })
    : "Récemment";

  return (
    <div className="flex items-center justify-between p-3 bg-white hover:bg-slate-50 border-b border-gray-100 transition-all gap-3">
      {/* Partie Gauche : Icône + Label */}
      <div className="flex items-center gap-3">
        <div
          className={`p-2 rounded-full ${
            isSelfTransfer
              ? "bg-blue-50 text-blue-600"
              : isPositive
                ? "bg-emerald-50 text-emerald-600"
                : "bg-rose-50 text-rose-600"
          }`}
        >
          {isSelfTransfer ? (
            <PlusCircle size={18} />
          ) : isPositive ? (
            <ArrowDownLeft size={18} />
          ) : (
            <ArrowUpRight size={18} />
          )}
        </div>

        <div className="flex flex-col">
          <span className="text-sm font-semibold text-gray-800 leading-none mb-1">
            {operation.label}
          </span>
          <span className="text-xs text-gray-400 capitalize">
            {relativeDate}
          </span>
        </div>
      </div>

      {/* Partie Droite : Montant + Tiers */}
      <div className="flex flex-col items-end min-w-30">
        <span
          className={`text-base font-bold ${
            isPositive ? "text-emerald-600" : "text-rose-600"
          }`}
        >
          {operation.sign === "plus" ? "+" : "-"}
          {operation.amount.toLocaleString("fr-FR", {
            minimumFractionDigits: 2,
          })}
          €
        </span>

        <span className="text-[10px] text-gray-400 italic truncate max-w-35">
          {isSelfTransfer
            ? "Alimentation compte"
            : isPositive
              ? `De: ${operation.mail}`
              : `Vers: ${operation.receiverMail}`}
        </span>
      </div>
    </div>
  );
};
