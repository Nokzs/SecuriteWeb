import { useState } from "react";
import { Search } from "lucide-react";

interface SyndicSearchProps {
  onSearch: (query: string) => void;
  placeholder?: string;
}

export function SyndicSearch({ onSearch, placeholder = "Chercher un syndic..." }: SyndicSearchProps) {
  const [searchQuery, setSearchQuery] = useState("");

  const handleSearch = (value: string) => {
    setSearchQuery(value);
    onSearch(value);
  };

  return (
    <div className="w-full max-w-2xl mx-auto mb-8">
      <div className="relative">
        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
        <input
          type="text"
          placeholder={placeholder}
          value={searchQuery}
          onChange={(e) => handleSearch(e.target.value)}
          className="w-full pl-10 pr-4 py-3 border-2 border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 transition"
        />
      </div>
    </div>
  );
}
