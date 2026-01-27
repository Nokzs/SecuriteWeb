import type { Dispatch, SetStateAction } from "react";

type SearchBarProps = {
  setSearchTerm: (term: string) => void;
  searchTerm: string;
  setFilter: Dispatch<SetStateAction<{ limit: number; page: number }>>;
};
export function SearchBar({
  setSearchTerm,
  searchTerm,
  setFilter,
}: SearchBarProps) {
  return (
    <input
      type="text"
      placeholder="Rechercher par nom ou adresse..."
      className="block w-full pl-10 pr-3 py-2 border border-slate-200 rounded-lg leading-5 bg-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm transition-all"
      value={searchTerm}
      onChange={(e) => {
        setSearchTerm(e.target.value);
        setFilter((filter) => {
          return { ...filter, page: 0 };
        });
      }}
    />
  );
}
