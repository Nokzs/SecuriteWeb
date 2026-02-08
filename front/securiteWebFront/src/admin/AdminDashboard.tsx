import { useQuery } from "@tanstack/react-query";
import { Users, Building, AlertTriangle, Vote, Trash2 } from "lucide-react";
import { useSecureFetch } from "../hooks/secureFetch"; // Vérifie ton chemin d'import
const API_BASE = import.meta.env.VITE_APIURL;

export function AdminDashboard() {
  const secureFetch = useSecureFetch();

  // 1. Charger les stats
  const { data: stats } = useQuery({
    queryKey: ["adminStats"],
    queryFn: async () => {
      const res = await secureFetch(`${API_BASE}/admin/stats`); 
      if (!res.ok) return {}; 
      return await res.json();
    }
  });

  // 2. Charger les utilisateurs
  const { data: users, refetch } = useQuery({
    queryKey: ["adminUsers"],
    queryFn: async () => {
      const res = await secureFetch(`${API_BASE}/admin/users`);
      if (!res.ok) return []; 
      const data = await res.json();
      return Array.isArray(data) ? data : [];
    }
  });

  // 3. Fonction de suppression
  const handleDelete = async (id: string) => {
    if (!confirm("Êtes-vous sûr de vouloir bannir cet utilisateur ?")) return;
    
    try {
        await secureFetch(`${API_BASE}/admin/users/${id}`, { method: "DELETE" });
        refetch(); // Rafraîchir la liste
    } catch (e) {
        alert("Erreur lors de la suppression");
    }
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <header>
          <h1 className="text-3xl font-bold text-slate-800">Panneau d'Administration</h1>
          <p className="text-slate-500">Vue globale de l'activité de la plateforme.</p>
      </header>

      {/* --- CARTES STATS --- */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <StatCard title="Utilisateurs" value={stats?.totalUsers} icon={<Users />} color="bg-blue-500" />
        <StatCard title="Immeubles" value={stats?.totalBuildings} icon={<Building />} color="bg-indigo-500" />
        <StatCard title="Incidents" value={stats?.totalIncidents} icon={<AlertTriangle />} color="bg-orange-500" />
        <StatCard title="Votes Actifs" value={stats?.activeVotes} icon={<Vote />} color="bg-green-500" />
      </div>

      {/* --- TABLEAU UTILISATEURS --- */}
      <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        <div className="p-6 border-b border-slate-200 bg-slate-50/50">
          <h2 className="text-xl font-bold text-slate-800 flex items-center gap-2">
            <Users size={20} className="text-indigo-600"/>
            Gestion des Utilisateurs
          </h2>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-600">
            <thead className="bg-slate-50 text-slate-800 font-semibold uppercase text-xs tracking-wider">
              <tr>
                <th className="px-6 py-4">ID</th>
                <th className="px-6 py-4">Email</th>
                <th className="px-6 py-4">Rôle</th>
                <th className="px-6 py-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {users?.map((user: any) => (
                <tr key={user.id} className="hover:bg-slate-50 transition">
                  <td className="px-6 py-4 font-mono text-xs text-slate-400">{user.id.substring(0, 8)}...</td>
                  <td className="px-6 py-4 font-medium text-slate-900">{user.email}</td>
                  <td className="px-6 py-4">
                    <span className={`px-2.5 py-1 rounded-full text-xs font-bold border ${
                      user.role === 'ADMIN' ? 'bg-purple-50 text-purple-700 border-purple-200' :
                      user.role === 'SYNDIC' ? 'bg-blue-50 text-blue-700 border-blue-200' : 
                      'bg-green-50 text-green-700 border-green-200'
                    }`}>
                      {user.role}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    {user.role !== 'ADMIN' && (
                      <button 
                        onClick={() => handleDelete(user.id)}
                        className="text-slate-400 hover:text-red-600 hover:bg-red-50 p-2 rounded-lg transition-all"
                        title="Bannir l'utilisateur"
                      >
                        <Trash2 size={18} />
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function StatCard({ title, value, icon, color }: any) {
  return (
    <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200 flex items-center gap-4 transition-transform hover:scale-[1.02]">
      <div className={`${color} text-white p-3 rounded-lg shadow-md`}>
        {icon}
      </div>
      <div>
        <p className="text-slate-500 text-sm font-medium uppercase tracking-wide">{title}</p>
        <p className="text-3xl font-bold text-slate-800">{value || 0}</p>
      </div>
    </div>
  );
}