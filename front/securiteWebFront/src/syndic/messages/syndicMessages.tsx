import { useState, useEffect } from "react";
import { Mail, Archive, Trash2, CheckCircle, Clock } from "lucide-react";
import { useQuery, useMutation, useQueryClient, keepPreviousData } from "@tanstack/react-query";
import { useSecureFetch } from "../../hooks/secureFetch";
import { PaginationController } from "../../component/PaginationController";
import type { Page } from "../../types/pagination";
import { userStore } from "../../store/userStore";

interface Message {
  id: string;
  senderFirstName: string;
  senderLastName: string;
  senderPhone: string;
  senderEmail: string | null;
  messageContent: string;
  isRead: boolean;
  isArchived: boolean;
  createdAt: string;
}

const API_URL = import.meta.env.VITE_APIURL;

export function SyndicMessages() {
  const secureFetch = useSecureFetch();
  const queryClient = useQueryClient();
  const user = userStore((s) => s.user);
  const get = userStore((s) => s.get);
  const parsedUser = user ? get(user) : null;

  const [selectedMessage, setSelectedMessage] = useState<Message | null>(null);
  const [filter, setFilter] = useState({ limit: 10, page: 0 });
  const [filterType, setFilterType] = useState<"all" | "unread" | "archived">("all");

  const { data, isLoading } = useQuery<Page<Message>>({
    queryKey: ["syndicMessages", parsedUser?.uuid, filter, filterType],
    queryFn: async () => {
      const res = await secureFetch(
        `${API_URL}/syndics/messages?limit=${filter.limit}&page=${filter.page}&type=${filterType}`
      );
      if (!res.ok) throw new Error("Erreur réseau");
      return await res.json();
    },
    enabled: !!parsedUser?.uuid,
    placeholderData: keepPreviousData,
  });

  const markAsReadMutation = useMutation({
    mutationFn: async (messageId: string) => {
      await secureFetch(`${API_URL}/syndics/messages/${messageId}/read`, { method: "PATCH" });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["syndicMessages"] });
    },
  });

  const archiveMutation = useMutation({
    mutationFn: async (messageId: string) => {
      await secureFetch(`${API_URL}/syndics/messages/${messageId}/archive`, { method: "PATCH" });
    },
    onSuccess: () => {
      setSelectedMessage(null);
      queryClient.invalidateQueries({ queryKey: ["syndicMessages"] });
    },
  });

  useEffect(() => {
    if (selectedMessage && !selectedMessage.isRead) {
      markAsReadMutation.mutate(selectedMessage.id);
      setSelectedMessage({ ...selectedMessage, isRead: true });
    }
  }, [selectedMessage?.id]);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("fr-FR", {
      day: "numeric", month: "short", hour: "2-digit", minute: "2-digit",
    });
  };

  if (isLoading) return <div className="p-12 text-center"><span className="loading loading-spinner loading-lg text-indigo-600"></span></div>;

  return (
    <div className="max-w-7xl mx-auto p-6">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-2xl font-black text-slate-800 tracking-tight">Gestion des messages</h1>
        <div className="flex gap-2 bg-slate-100 p-1 rounded-xl">
          {(["all", "unread", "archived"] as const).map((type) => (
            <button
              key={type}
              onClick={() => { setFilterType(type); setFilter(f => ({ ...f, page: 0 })); }}
              className={`px-4 py-2 rounded-lg text-xs font-bold transition-all ${
                filterType === type ? "bg-white text-indigo-600 shadow-sm" : "text-slate-500 hover:text-slate-700"
              }`}
            >
              {type === "all" ? "Tous" : type === "unread" ? "Non lus" : "Archivés"}
            </button>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* LISTE DES MESSAGES */}
        <div className="lg:col-span-5 space-y-3">
          {data?.content.map((msg) => (
            <div
              key={msg.id}
              onClick={() => setSelectedMessage(msg)}
              className={`relative p-4 rounded-2xl border-2 transition-all cursor-pointer ${
                selectedMessage?.id === msg.id
                  ? "border-indigo-500 bg-indigo-50/40"
                  : "border-slate-100 bg-white hover:border-slate-200 shadow-sm"
              }`}
            >
              {!msg.isRead && (
                <div className="absolute top-4 right-4 w-2 h-2 bg-indigo-600 rounded-full"></div>
              )}
              <div className="flex items-center gap-3">
                <div className={`p-2 rounded-lg ${msg.isRead ? "bg-slate-50 text-slate-400" : "bg-indigo-100 text-indigo-600"}`}>
                  <Mail size={16} />
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className={`font-bold truncate ${msg.isRead ? "text-slate-500" : "text-slate-800"}`}>
                    {msg.senderFirstName} {msg.senderLastName}
                  </h3>
                  <p className="text-[10px] text-slate-400 flex items-center gap-1">
                    <Clock size={10} /> {formatDate(msg.createdAt)}
                  </p>
                </div>
              </div>
            </div>
          ))}
          {data && data.totalPages > 1 && (
             <PaginationController pageData={data} onPageChange={(p) => setFilter(f => ({ ...f, page: p }))} />
          )}
        </div>

        {/* PANNEAU DE DETAILS */}
        <div className="lg:col-span-7">
          {selectedMessage ? (
            <div className="bg-white rounded-3xl border border-slate-200 overflow-hidden sticky top-6 shadow-sm">
              <div className="p-8 border-b border-slate-50 bg-slate-50/40">
                <div className="flex justify-between items-start mb-6">
                  <div className="flex items-center gap-4">
                    <div className="h-12 w-12 bg-slate-800 rounded-xl flex items-center justify-center text-white font-bold text-lg">
                      {selectedMessage.senderFirstName[0]}{selectedMessage.senderLastName[0]}
                    </div>
                    <div>
                      <h2 className="text-xl font-bold text-slate-900 leading-tight">
                        {selectedMessage.senderFirstName} {selectedMessage.senderLastName}
                      </h2>
                      <p className="text-sm text-slate-500 font-medium">{selectedMessage.senderEmail || "Pas d'email"}</p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button 
                      onClick={() => archiveMutation.mutate(selectedMessage.id)}
                      className="p-2.5 text-slate-400 hover:text-amber-600 hover:bg-amber-100/50 rounded-xl transition-all"
                      title="Archiver"
                    >
                      <Archive size={20} />
                    </button>
                    <button 
                      className="p-2.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-xl transition-all"
                      title="Supprimer"
                    >
                      <Trash2 size={20} />
                    </button>
                  </div>
                </div>

                <div className="flex gap-8 text-sm">
                   <div>
                      <p className="text-[10px] uppercase font-bold text-slate-400 mb-1">Téléphone</p>
                      <p className="text-slate-700 font-bold">{selectedMessage.senderPhone}</p>
                   </div>
                   <div>
                      <p className="text-[10px] uppercase font-bold text-slate-400 mb-1">Date de réception</p>
                      <p className="text-slate-700 font-bold">{formatDate(selectedMessage.createdAt)}</p>
                   </div>
                   <div>
                      <p className="text-[10px] uppercase font-bold text-slate-400 mb-1">Statut</p>
                      <p className="flex items-center gap-1 text-indigo-600 font-bold">
                        <CheckCircle size={14} /> Message lu
                      </p>
                   </div>
                </div>
              </div>

              <div className="p-8">
                <h4 className="text-[10px] uppercase font-black text-slate-300 tracking-widest mb-6">Contenu du message</h4>
                <div className="text-slate-700 leading-relaxed text-lg whitespace-pre-wrap font-medium">
                  {selectedMessage.messageContent}
                </div>
              </div>
            </div>
          ) : (
            <div className="h-[400px] flex flex-col items-center justify-center bg-slate-50/50 rounded-3xl border-2 border-dashed border-slate-200">
              <Mail className="text-slate-200 mb-4" size={56} />
              <p className="text-slate-400 font-semibold text-lg">Sélectionnez une demande pour l'afficher</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}