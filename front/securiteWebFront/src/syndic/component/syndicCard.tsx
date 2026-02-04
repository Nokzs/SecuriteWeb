import { Mail, Phone } from "lucide-react";

interface SyndicCardProps {
  id: string;
  name: string;
  email: string;
  phone: string;
  address?: string;
  buildingsCount?: number;
  onContact: () => void;
}

export function SyndicCard({
  name,
  email,
  phone,
  address,
  buildingsCount,
  onContact,
}: SyndicCardProps) {
  return (
    <div className="bg-white rounded-lg shadow-md p-6 border border-gray-200 hover:shadow-lg transition">
      <div className="mb-4">
        <h3 className="text-xl font-bold text-gray-800 mb-1">{name}</h3>
        {address && <p className="text-sm text-gray-600">{address}</p>}
      </div>

      <div className="space-y-2 mb-4">
        <div className="flex items-center gap-2 text-gray-700">
          <Mail size={16} />
          <a href={`mailto:${email}`} className="text-blue-600 hover:underline break-all">
            {email}
          </a>
        </div>
        <div className="flex items-center gap-2 text-gray-700">
          <Phone size={16} />
          <a href={`tel:${phone}`} className="text-blue-600 hover:underline">
            {phone}
          </a>
        </div>
      </div>

      {buildingsCount !== undefined && (
        <p className="text-sm text-gray-600 mb-4">
          <span className="font-semibold">{buildingsCount}</span> bâtiment(s) géré(s)
        </p>
      )}

      <button
        onClick={onContact}
        className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-4 rounded-lg transition"
      >
        Envoyer un message
      </button>
    </div>
  );
}
