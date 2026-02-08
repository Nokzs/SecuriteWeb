import React, { useState } from "react";
import { X } from "lucide-react";

interface AddMoneyModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (amount: number) => void;
}

export const AddMoneyModal = ({
  isOpen,
  onClose,
  onConfirm,
}: AddMoneyModalProps) => {
  const [amount, setAmount] = useState("");

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const parsedAmount = parseFloat(amount);
    if (!isNaN(parsedAmount) && parsedAmount > 0) {
      onConfirm(parsedAmount);
      setAmount("");
    }
  };

  return (
    <div className="absolute inset-0 z-50 bg-white/90 backdrop-blur-md rounded-[2.5rem] p-8 flex flex-col animate-in fade-in slide-in-from-bottom-10 duration-300">
      <div className="flex justify-end">
        <button
          onClick={onClose}
          className="p-2 text-slate-400 hover:text-slate-600 transition-colors"
        >
          <X size={24} />
        </button>
      </div>

      <div className="flex-1 flex flex-col justify-center">
        <h2 className="text-2xl font-black text-slate-800 mb-2">
          Ajouter des fonds
        </h2>
        <p className="text-slate-500 text-sm mb-8">
          Créditez votre compte instantanément.
        </p>

        <form onSubmit={handleSubmit}>
          <div className="relative mb-6">
            <span className="absolute left-4 top-1/2 -translate-y-1/2 text-2xl font-bold text-slate-400">
              €
            </span>
            <input
              autoFocus
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="0.00"
              className="w-full bg-slate-100 border-none rounded-2xl py-6 pl-12 pr-4 text-3xl font-black text-slate-800 focus:ring-2 focus:ring-indigo-500 transition-all outline-none"
            />
          </div>
          <button
            type="submit"
            className="w-full bg-indigo-600 text-white py-5 rounded-2xl font-bold text-lg shadow-xl shadow-indigo-200 hover:bg-indigo-700 active:scale-95 transition-all"
          >
            Confirmer le dépôt
          </button>
        </form>
      </div>
    </div>
  );
};
