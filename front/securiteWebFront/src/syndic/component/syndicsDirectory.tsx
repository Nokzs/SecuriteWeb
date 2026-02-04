import { useState } from "react";
import { SyndicCard } from "./syndicCard";
import { SyndicSearch } from "./syndicSearch";
import { ContactSyndicForm } from "./contactSyndicForm";
import { Loader } from "lucide-react";
import { API_BASE } from "../../config/urls";
import { useSecureFetch } from "../../hooks/secureFetch";
import { useQuery, keepPreviousData } from "@tanstack/react-query";
import { PaginationController } from "../../component/PaginationController";
import type { Page } from "../../types/pagination";

interface Syndic {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  address?: string;
  buildingsCount?: number;
}

interface ContactFormData {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  message: string;
}

export function SyndicsDirectory() {
  const secureFetch = useSecureFetch();
  const [selectedSyndic, setSelectedSyndic] = useState<Syndic | null>(null);
  const [showContactForm, setShowContactForm] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [filter, setFilter] = useState<{ limit: number; page: number }>({
    limit: 6,
    page: 0,
  });

  const onPageChange = (newPage: number) => {
    setFilter((filter) => ({ ...filter, page: newPage }));
  };

  const { data, isLoading, error } = useQuery<Page<Syndic>>({
    queryKey: ["syndics", filter, searchTerm],
    queryFn: async () => {
      const response = await fetch(
        `${API_BASE}/syndics?limit=${filter.limit}&page=${filter.page}&search=${encodeURIComponent(searchTerm)}`,
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
          },
        },
      );

      if (!response.ok) {
        throw new Error("Erreur lors de la récupération des syndics");
      }

      return (await response.json()) as Page<Syndic>;
    },
    placeholderData: keepPreviousData,
  });

  const handleSearch = (query: string) => {
    setSearchTerm(query);
    setFilter((filter) => ({ ...filter, page: 0 }));
  };

  const handleContactClick = (syndic: Syndic) => {
    setSelectedSyndic(syndic);
    setShowContactForm(true);
  };

  const handleFormSubmit = async (data: ContactFormData) => {
    if (!selectedSyndic) return;

    try {
      const response = await secureFetch(
        `${API_BASE}/syndics/${selectedSyndic.id}/contact`,

        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            firstName: data.firstName,
            lastName: data.lastName,
            email: data.email,
            phone: data.phone,
            message: data.message,
          }),
        },
      );

      if (!response.ok) {
        throw new Error("Erreur lors de l'envoi du message");
      }

      return response.json();
    } catch (err) {
      throw err instanceof Error ? err : new Error("Une erreur est survenue");
    }
  };

  if (isLoading && !data) {
    return (
      <div className="flex justify-center items-center py-12">
        <Loader className="animate-spin text-blue-600" size={32} />
      </div>
    );
  }

  const errorMessage =
    error instanceof Error ? error.message : "Une erreur est survenue";

  return (
    <div className="w-full">
      <SyndicSearch
        onSearch={handleSearch}
        placeholder="Chercher par nom, prénom, email ou téléphone..."
      />

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg mb-6 max-w-4xl mx-auto">
          {errorMessage}
        </div>
      )}

      {data && (
        <>
          <div className="mb-6 max-w-6xl mx-auto">
            <p className="text-gray-600 text-center">
              {data.totalElements} syndic{data.totalElements !== 1 ? "s" : ""}{" "}
              trouvé
              {data.totalElements !== 1 ? "s" : ""}
            </p>
          </div>

          {data.content.length === 0 ? (
            <div className="text-center py-12 max-w-4xl mx-auto">
              <p className="text-gray-600 text-lg">
                Aucun syndic ne correspond à votre recherche.
              </p>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-6xl mx-auto mb-8">
                {data.content.map((syndic) => (
                  <SyndicCard
                    key={syndic.id}
                    id={syndic.id}
                    name={`${syndic.firstName} ${syndic.lastName}`}
                    email={syndic.email}
                    phone={syndic.phone}
                    address={syndic.address}
                    buildingsCount={syndic.buildingsCount}
                    onContact={() => handleContactClick(syndic)}
                  />
                ))}
              </div>
              <div className="max-w-6xl mx-auto">
                <PaginationController<Syndic>
                  onPageChange={onPageChange}
                  pageData={data}
                />
              </div>
            </>
          )}
        </>
      )}

      {showContactForm && selectedSyndic && (
        <ContactSyndicForm
          syndicName={`${selectedSyndic.firstName} ${selectedSyndic.lastName}`}
          onClose={() => {
            setShowContactForm(false);
            setSelectedSyndic(null);
          }}
          onSubmit={handleFormSubmit}
        />
      )}
    </div>
  );
}
